package com.codeshaper.jello.engine.render;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL;

import com.codeshaper.jello.engine.Color;
import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.SceneManager;
import com.codeshaper.jello.engine.asset.Scene;
import com.codeshaper.jello.engine.component.Camera;
import com.codeshaper.jello.engine.component.JelloComponent;

import static org.lwjgl.opengl.GL30.*;

public class Renderer {
	
	public static final String PROJECTION_MATRIX = "projectionMatrix";
	public static final String VIEW_MATRIX = "viewMatrix";
	public static final String GAME_OBJECT_MATRIX = "modelMatrix";
	public static final String TXT_SAMPLER = "txtSampler";

	public final ShaderProgram shaderProgram;
    public final UniformsMap uniformsMap;
        
	public Renderer() {
		GL.createCapabilities();
		
        glEnable(GL_DEPTH_TEST);  
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        this.shaderProgram = new ShaderProgram(
        		ShaderProgram.ShaderModuleData.fromResources("/builtin/shaders/scene.vert", GL_VERTEX_SHADER),
        		ShaderProgram.ShaderModuleData.fromResources("/builtin/shaders/scene.frag", GL_FRAGMENT_SHADER));
        
        this.uniformsMap = new UniformsMap(shaderProgram.getProgramId());
        this.uniformsMap.createUniform(PROJECTION_MATRIX);
        this.uniformsMap.createUniform(VIEW_MATRIX);
        this.uniformsMap.createUniform(GAME_OBJECT_MATRIX);
        this.uniformsMap.createUniform(TXT_SAMPLER);
   	}

	public void cleanup() {
		this.shaderProgram.cleanup();
	}

	public void render(SceneManager sceneManager, Camera camera, Matrix4f viewMatrix, int w, int h) {
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
        
        for(Scene scene : sceneManager.getScenes()) {
    		for(GameObject obj : scene.getRootGameObjects()) {
    			matrix.translationRotateScale(obj.getPosition(), obj.getRotation(), obj.getScale());
                uniformsMap.setUniform(GAME_OBJECT_MATRIX, matrix);

        		for(JelloComponent component : obj.getAllComponents()) {
        			component.onRender();
        		}
        	}        
    	}

		glBindVertexArray(0);

		shaderProgram.unbind();
	}
}
