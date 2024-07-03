package com.codeshaper.jello.engine.render;

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
	private static final String GAME_OBJECT_MATRIX = "modelMatrix";;

	public final ShaderProgram shaderProgram;
    public final UniformsMap uniformsMap;

	public Renderer() {
		GL.createCapabilities();
		
        glEnable(GL_DEPTH_TEST);

		List<ShaderProgram.ShaderModuleData> shaderModuleDataList = new ArrayList<>();
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("resources/shaders/scene.vert", GL_VERTEX_SHADER));
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("resources/shaders/scene.frag", GL_FRAGMENT_SHADER));
        this.shaderProgram = new ShaderProgram(shaderModuleDataList);
        
        this.uniformsMap = new UniformsMap(shaderProgram.getProgramId());
        this.uniformsMap.createUniform(PROJECTION_MATRIX);
        this.uniformsMap.createUniform(GAME_OBJECT_MATRIX);
	}

	public void cleanup() {
		this.shaderProgram.cleanup();
	}

	public void render(ISceneProvider sceneProvider, Camera camera, int w, int h) {
		camera.refreshProjectionMatrix(w, h);
		
        Color clearColor = camera.backgroundColor;
        glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);        
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glViewport(0, 0, w, h);

		shaderProgram.bind();
		
        uniformsMap.setUniform(PROJECTION_MATRIX, camera.getProjectionMatrix());        
        
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
