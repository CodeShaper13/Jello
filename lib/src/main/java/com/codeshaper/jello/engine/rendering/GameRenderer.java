package com.codeshaper.jello.engine.rendering;

import static org.lwjgl.opengl.GL30.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.joml.Matrix4f;

import com.codeshaper.jello.engine.Color;
import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.Scene;
import com.codeshaper.jello.engine.SceneManager;
import com.codeshaper.jello.engine.asset.Material;
import com.codeshaper.jello.engine.asset.Shader;
import com.codeshaper.jello.engine.component.Camera;
import com.codeshaper.jello.engine.component.JelloComponent;
import com.codeshaper.jello.engine.component.Renderer;
import com.codeshaper.jello.engine.database.AssetDatabase;

public class GameRenderer {
		
	public static final String PROJECTION_MATRIX = "projectionMatrix";
	public static final String VIEW_MATRIX = "viewMatrix";
	public static final String GAME_OBJECT_MATRIX = "modelMatrix";
    
	private final Shader errorShader;
	private final HashMap<Material, List<Renderer>> instructions;
        
	public GameRenderer() {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        this.errorShader = (Shader) AssetDatabase.getInstance().getAsset("builtin/shaders/error.shader");
        this.instructions = new HashMap<Material, List<Renderer>>(256);
   	}
	
	public void render(SceneManager sceneManager, Camera camera, Matrix4f viewMatrix, int w, int h) {
		camera.refreshProjectionMatrix(w, h);
		
        Color clearColor = camera.backgroundColor;
        glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);        
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glViewport(0, 0, w, h);
        glEnable(GL_DEPTH_TEST);
		
		this.instructions.clear();
		
		for(Scene scene : sceneManager.getScenes()) {
    		for(GameObject obj : scene.getRootGameObjects()) {
    			this.createInstructionsRecursively(obj);
        	}        
    	}	
		
		for(Material material : this.instructions.keySet()) {
			Shader shader = material.getShader();
			
			if(shader == null || shader.isInvalid()) {
				shader = this.errorShader;
			}
			
			ShaderProgram program = shader.getProgram();
			program.bind();
			
			program.setUniform(PROJECTION_MATRIX, camera.getProjectionMatrix());   
	        program.setUniform(VIEW_MATRIX, viewMatrix);
	        
	        material.setUniforms();
	        
	        List<Renderer> renderers = this.instructions.get(material);
	        for(Renderer renderer : renderers) {
	        	GameObject obj = renderer.gameObject;	        	
	        	program.setUniform(GAME_OBJECT_MATRIX, obj.getWorldMatrix());
                
                renderer.onRender();
	        }
	        
			program.unbind();
		}
		
		glBindVertexArray(0);
	}
	
	private void createInstructionsRecursively(GameObject gameObject) {
		if(!gameObject.isEnabled()) {
			return;
		}
		
		for(JelloComponent component : gameObject.getAllComponents()) {
			if(component.isEnabled()) {
				if(component instanceof Renderer) {
					Renderer renderer = (Renderer)component;
					Material material = renderer.getMaterial();
					if(material != null) {
    					this.addInstruction(material, renderer);
    				}	
				}
			}
		}
		
		for(GameObject child : gameObject.getChildren()) {
			this.createInstructionsRecursively(child);
		}
	}
		
	private void addInstruction(Material material, Renderer renderer) {
		if(this.instructions.containsKey(material)) {
			this.instructions.get(material).add(renderer);
		} else {
			List<Renderer> list = new ArrayList<Renderer>();
			list.add(renderer);
			this.instructions.put(material, list);
		}
	}
}
