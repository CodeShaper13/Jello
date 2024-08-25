package com.codeshaper.jello.engine.rendering;

import static org.lwjgl.opengl.GL30.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import com.codeshaper.jello.engine.AssetLocation;
import com.codeshaper.jello.engine.Color;
import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.JelloComponent;
import com.codeshaper.jello.engine.Scene;
import com.codeshaper.jello.engine.SceneManager;
import com.codeshaper.jello.engine.asset.Material;
import com.codeshaper.jello.engine.asset.Shader;
import com.codeshaper.jello.engine.database.AssetDatabase;
import com.codeshaper.jello.engine.rendering.shader.ShaderData;
import com.codeshaper.jello.engine.rendering.shader.ShaderProgram;
import com.codeshaper.jello.engine.rendering.shader.ShaderData.CullMode;

public class GameRenderer {

	public static final String PROJECTION_MATRIX = "projectionMatrix";
	public static final String VIEW_MATRIX = "viewMatrix";
	public static final String GAME_OBJECT_MATRIX = "modelMatrix";
	public static final String _FOG_COLOR = "_fog.color";
	public static final String _FOG_DENSITY = "_fog.density";

	
	private final Shader errorShader;
	private final HashMap<Material, List<Renderer>> instructions;

	public GameRenderer() {
		glEnable(GL_BLEND);
		glBlendEquation(GL_FUNC_ADD);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		this.errorShader = (Shader) AssetDatabase.getInstance().getAsset(new AssetLocation("builtin/shaders/error.shader"));
		this.instructions = new HashMap<Material, List<Renderer>>(256);
	}

	public void render(SceneManager sceneManager, Camera camera, Matrix4f viewMatrix, int windowWidth,
			int windowHeight) {
		Vector2f viewportPos = camera.viewportPosition;
		Vector2f viewportSize = camera.viewportSize;
		float viewportWidth = windowWidth * viewportSize.x;
		float viewportHeight = windowHeight * viewportSize.y;
		glViewport(
				Math.round(viewportPos.x * windowWidth),
				Math.round(viewportPos.y * windowHeight),
				Math.round(viewportWidth),
				Math.round(viewportHeight));
		camera.refreshProjectionMatrix(viewportWidth, viewportHeight);

		Color clearColor = camera.backgroundColor;
		glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		this.instructions.clear();

		for(int i = 0; i < sceneManager.getSceneCount(); i++) {
			Scene scene = sceneManager.getScene(i);
			for (GameObject obj : scene.getRootGameObjects()) {
				this.createInstructionsRecursively(obj);
			}
		}

		for (Material material : this.instructions.keySet()) {
			Shader shader = material.getShader();

			if (shader == null || shader.isInvalid()) {
				shader = this.errorShader;
			}

			ShaderData data = shader.getData();

			if (data.depth_test) {
				glEnable(GL_DEPTH_TEST);
			} else {
				glDisable(GL_DEPTH_TEST);
			}

			if (data.culling == CullMode.OFF) {
				glDisable(GL_CULL_FACE);
			} else {
				glEnable(GL_CULL_FACE);
				if (data.culling == null || data.culling == CullMode.BACK) {
					glCullFace(GL_BACK);
				} else if (data.culling == CullMode.FRONT) {
					glCullFace(GL_FRONT);
				} else {
					glCullFace(GL_FRONT_AND_BACK);
				}
			}

			ShaderProgram program = shader.getProgram();
			program.bind();

			program.setUniform(PROJECTION_MATRIX, camera.getProjectionMatrix());
			program.setUniform(VIEW_MATRIX, viewMatrix);
			program.setUniform(_FOG_COLOR, camera.fogColor.toVector3f());
	        program.setUniform(_FOG_DENSITY, camera.fogDensity);
	        
			material.setUniforms();

			List<Renderer> renderers = this.instructions.get(material);
			for (Renderer renderer : renderers) {
				program.setUniform(GAME_OBJECT_MATRIX, renderer.gameObject().getWorldMatrix());

				renderer.onRender();
			}

			program.unbind();
		}

		glBindVertexArray(0);
	}

	private void createInstructionsRecursively(GameObject gameObject) {
		if (!gameObject.isActive()) {
			return;
		}

		for (JelloComponent component : gameObject.getAllComponents()) {
			if (component.isEnabled()) {
				if (component instanceof Renderer) {
					Renderer renderer = (Renderer) component;
					Material material = renderer.getMaterial();
					if (material != null) {
						this.addInstruction(material, renderer);
					}
				}
			}
		}

		for (GameObject child : gameObject.getChildren()) {
			this.createInstructionsRecursively(child);
		}
	}

	private void addInstruction(Material material, Renderer renderer) {
		if (this.instructions.containsKey(material)) {
			this.instructions.get(material).add(renderer);
		} else {
			List<Renderer> list = new ArrayList<Renderer>();
			list.add(renderer);
			this.instructions.put(material, list);
		}
	}
}
