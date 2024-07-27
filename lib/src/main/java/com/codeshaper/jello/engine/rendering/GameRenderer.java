package com.codeshaper.jello.engine.rendering;

import static org.lwjgl.opengl.GL30.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL;

import com.codeshaper.jello.engine.Color;
import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.Scene;
import com.codeshaper.jello.engine.SceneManager;
import com.codeshaper.jello.engine.asset.Material;
import com.codeshaper.jello.engine.asset.Shader;
import com.codeshaper.jello.engine.component.Camera;
import com.codeshaper.jello.engine.component.JelloComponent;
import com.codeshaper.jello.engine.component.Renderer;

public class GameRenderer {
	
	public static final String PROJECTION_MATRIX = "projectionMatrix";
	public static final String VIEW_MATRIX = "viewMatrix";
	public static final String GAME_OBJECT_MATRIX = "modelMatrix";
	public static final String TXT_SAMPLER = "txtSampler";
    
	private final HashMap<Material, List<Renderer>> instructions;
        
	public GameRenderer() {
		GL.createCapabilities();
		
        glEnable(GL_DEPTH_TEST);  
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        this.instructions = new HashMap<Material, List<Renderer>>(256);
   	}
	
	public void render(SceneManager sceneManager, Camera camera, Matrix4f viewMatrix, int w, int h) {
		camera.refreshProjectionMatrix(w, h);
		
        Color clearColor = camera.backgroundColor;
        glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);        
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glViewport(0, 0, w, h);
		
		this.instructions.clear();
		
		for(Scene scene : sceneManager.getScenes()) {
    		for(GameObject obj : scene.getRootGameObjects()) {
    			if(!obj.isEnabled()) {
    				continue;
    			}
    			
        		for(JelloComponent component : obj.getAllComponents()) {
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
        	}        
    	}	
		
        Matrix4f matrix = new Matrix4f();
		for(Material material : this.instructions.keySet()) {
			Shader shader = material.getShader();
			ShaderProgram program = shader.getProgram();
			program.bind();
			
			UniformsMap uniforms = shader.getUniformMap();
	        uniforms.setUniform(PROJECTION_MATRIX, camera.getProjectionMatrix());   
	        uniforms.setUniform(VIEW_MATRIX, viewMatrix);
	        uniforms.setUniform(TXT_SAMPLER, 0);
	        
	        material.bindTextures();
	        // TODO bind textures and other stuff specified in the material.
	        
	        List<Renderer> renderers = this.instructions.get(material);
	        for(Renderer renderer : renderers) {
	        	GameObject obj = renderer.gameObject;
    			matrix.translationRotateScale(obj.getPosition(), obj.getRotation(), obj.getScale());
                uniforms.setUniform(GAME_OBJECT_MATRIX, matrix);
                
                renderer.onRender();
	        }
	        
			program.unbind();
		}
		
		glBindVertexArray(0);
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
