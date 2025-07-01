package com.codeshaper.jello.engine.rendering;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL30.*;

import java.util.ArrayList;
import java.util.Collections;
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
import com.codeshaper.jello.engine.asset.Mesh;
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
	private final List<RenderInstruction> instructions;

	private class RenderInstruction {

		public final Material material;
		public final List<Renderer> renderers;

		public RenderInstruction(Material material) {
			this.material = material;
			this.renderers = new ArrayList<Renderer>();
		}

		public int getRenderOrder() {
			return this.material != null ? this.material.renderOrder : 0;
		}
	}

	public GameRenderer() {
		glEnable(GL_BLEND);
		glBlendEquation(GL_FUNC_ADD);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		this.errorShader = (Shader) AssetDatabase.getInstance()
				.getAsset(new AssetLocation("builtin/shaders/error.shader"));
		this.instructions = new ArrayList<GameRenderer.RenderInstruction>();
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
		camera.setSize(viewportWidth, viewportHeight);

		CameraClearMode clearMode = camera.clearMode;
		switch (clearMode) {
		case COLOR:
			Color clearColor = camera.backgroundColor;
			glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			break;
		case SKYBOX:
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			this.renderSkybox(camera, viewMatrix);;
			break;
		case DEPTH:
			glClear(GL_DEPTH_BUFFER_BIT);
			break;
		case DONT_CLEAR:
			// Don't do anything.
			break;
		}

		this.instructions.clear();

		for (int i = 0; i < sceneManager.getSceneCount(); i++) {
			Scene scene = sceneManager.getScene(i);
			for (GameObject obj : scene.getRootGameObjects()) {
				this.createInstructionsRecursively(obj);
			}
		}

		Collections.sort(this.instructions, (m1, m2) -> {
			return Integer.compare(m1.getRenderOrder(), m2.getRenderOrder());
		});

		for (RenderInstruction c : this.instructions) {
			this.drawRenderInstruction(c, camera, viewMatrix);
		}

		glBindVertexArray(0);
	}
	
	private void renderSkybox(Camera camera, Matrix4f vm) {
		Mesh mesh = (Mesh) AssetDatabase.getInstance().getAsset(new AssetLocation("builtin/meshes/skybox.blend"));
	
		//glDepthMask(false);
				
		Shader shader = this.getShaderFromMaterial(camera.skybox);
		this.func(shader.getData());
		
		ShaderProgram program = shader.getProgram();
		program.bind();
		
		program.setUniform(PROJECTION_MATRIX, camera.getProjectionMatrix());
		
		Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.set(vm);
        viewMatrix.m30(0);
        viewMatrix.m31(0);
        viewMatrix.m32(0);
		program.setUniform(VIEW_MATRIX, viewMatrix);

		if (camera.skybox != null) {
			camera.skybox.setUniforms();
		}

		program.setUniform(GAME_OBJECT_MATRIX, new Matrix4f());
		
		glBindVertexArray(mesh.getVaoId());
		glDrawElements(GL_TRIANGLES, mesh.getVerticeCount(), GL_UNSIGNED_INT, 0);
		
		//glDepthMask(true);
	}
	
	private Shader getShaderFromMaterial(Material material) {
		if (material != null) {
			Shader matShader = material.getShader();
			if (matShader != null && !matShader.isInvalid()) {
				return matShader;
			}
		}
		
		return this.errorShader;
	}
	
	private void func(ShaderData data) {
		if (data.depth_test) {
			glEnable(GL_DEPTH_TEST);
		} else {
			glDisable(GL_DEPTH_TEST);
		}
		
		glDepthMask(data.enable_depth_mask);

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
	}
	
	private void drawRenderInstruction(RenderInstruction c, Camera camera, Matrix4f viewMatrix) {
		Material material = c.material;
		Shader shader = this.getShaderFromMaterial(material);
		this.func(shader.getData());

		ShaderProgram program = shader.getProgram();
		program.bind();

		program.setUniform(PROJECTION_MATRIX, camera.getProjectionMatrix());
		program.setUniform(VIEW_MATRIX, viewMatrix);
		program.setUniform(_FOG_COLOR, camera.fogColor.toVector3f());
		program.setUniform(_FOG_DENSITY, camera.fogDensity);

		if (material != null) {
			material.setUniforms();
		}

		List<Renderer> renderers = c.renderers;
		for (Renderer renderer : renderers) {
			program.setUniform(GAME_OBJECT_MATRIX, renderer.gameObject().getWorldMatrix());

			renderer.onRender(camera);
		}

		program.unbind();
	}

	private void createInstructionsRecursively(GameObject gameObject) {
		if (!gameObject.isActive()) {
			return;
		}

		for (JelloComponent component : gameObject.getAllComponents()) {
			if (component.isEnabled()) {
				if (component instanceof Renderer) {
					Renderer renderer = (Renderer) component;
					this.addInstruction(renderer.getMaterial(), renderer);
				}
			}
		}

		for (GameObject child : gameObject.getChildren()) {
			this.createInstructionsRecursively(child);
		}
	}

	private void addInstruction(Material material, Renderer renderer) {
		RenderInstruction instruction = null;
		for (RenderInstruction c1 : this.instructions) {
			if (c1.material == material) {
				instruction = c1;
				break;
			}
		}

		if (instruction == null) {
			instruction = new RenderInstruction(material);
			this.instructions.add(instruction);
		}

		instruction.renderers.add(renderer);
	}
}
