package com.codeshaper.jello.engine.render;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL;

import com.codeshaper.jello.engine.Color;
import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.ISceneProvider;
import com.codeshaper.jello.engine.asset.Scene;
import com.codeshaper.jello.engine.component.Camera;
import com.codeshaper.jello.engine.component.JelloComponent;

import static org.lwjgl.opengl.GL30.*;

public class Renderer {
	
	private static final String PROJECTION_MATRIX = "projectionMatrix";
	private static final String VIEW_MATRIX = "viewMatrix";
	private static final String GAME_OBJECT_MATRIX = "modelMatrix";
	private static final String TXT_SAMPLER = "txtSampler";

	public final ShaderProgram shaderProgram;
    public final UniformsMap uniformsMap;

	public Renderer() {
		GL.createCapabilities();
		
        glEnable(GL_DEPTH_TEST);

		List<ShaderProgram.ShaderModuleData> shaderModuleDataList = new ArrayList<>();
		
		Path vertPath = null;
		Path fragPath = null;
		try {
			vertPath = Paths.get(getClass().getResource("/builtin/shaders/scene.vert").toURI());
			fragPath = Paths.get(getClass().getResource("/builtin/shaders/scene.frag").toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData(vertPath, GL_VERTEX_SHADER));
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData(fragPath, GL_FRAGMENT_SHADER));
        this.shaderProgram = new ShaderProgram(shaderModuleDataList);
        
        this.uniformsMap = new UniformsMap(shaderProgram.getProgramId());
        this.uniformsMap.createUniform(PROJECTION_MATRIX);
        this.uniformsMap.createUniform(VIEW_MATRIX);
        this.uniformsMap.createUniform(GAME_OBJECT_MATRIX);
        this.uniformsMap.createUniform(TXT_SAMPLER);
	}

	public void cleanup() {
		this.shaderProgram.cleanup();
	}

	public void render(ISceneProvider sceneProvider, Camera camera, Matrix4f viewMatrix, int w, int h) {
		camera.refreshProjectionMatrix(w, h);
		
        Color clearColor = camera.backgroundColor;
        glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);        
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glViewport(0, 0, w, h);

		shaderProgram.bind();
		
        uniformsMap.setUniform(PROJECTION_MATRIX, camera.getProjectionMatrix());   
        uniformsMap.setUniform(VIEW_MATRIX, viewMatrix);
        uniformsMap.setUniform(TXT_SAMPLER, 0);
        
         Matrix4f matrix = new Matrix4f();
        
        for(Scene scene : sceneProvider.getScenes()) {
    		for(GameObject obj : scene.getRootGameObjects()) {
    			matrix.translationRotateScale(obj.getPosition(), obj.getRotation(), obj.getScale());
                uniformsMap.setUniform("modelMatrix", matrix);

        		for(JelloComponent component : obj.getAllComponents()) {
        			component.onRender();
        		}
        	}        
    	}

		glBindVertexArray(0);

		shaderProgram.unbind();
	}
}
