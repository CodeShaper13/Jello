package com.codeshaper.jello.engine.asset;

import static org.lwjgl.opengl.GL30.*;

import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.property.modifier.CreateAssetEntry;
import com.codeshaper.jello.editor.property.modifier.ExposeField;
import com.codeshaper.jello.editor.property.modifier.Space;
import com.codeshaper.jello.engine.AssetLocation;
import com.codeshaper.jello.engine.Color;

@CreateAssetEntry(fileName = "material", location = "Material")
public class Material extends SerializedJelloObject {

	@ExposeField
	private Shader shader = null;
	@Space
	public Texture diffuseTexture = null;
	public Color diffuseColor = Color.white;

	public Material(AssetLocation location) {
		super(location);
	}

	public Material() {
		super(null);
	}

	/**
	 * Gets the Material's Shader. If no Shader is set, Jello's default shader is
	 * returned.
	 * 
	 * @return
	 */
	public Shader getShader() {
		if(this.shader == null) {
	        return (Shader)JelloEditor.instance.assetDatabase.getAsset("builtin/shaders/default.shader");
		} else {
			return this.shader;
		}
	}
	
	public void bindTextures() {
		glActiveTexture(GL_TEXTURE0);
		if(this.diffuseTexture != null) {
	        this.diffuseTexture.bind();	
		} else {
			glBindTexture(GL_TEXTURE_2D, 0);
		}
	}
}
