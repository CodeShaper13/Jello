package com.codeshaper.jello.engine.asset;

import java.nio.file.Path;

import com.codeshaper.jello.editor.property.modifier.CreateAssetEntry;
import com.codeshaper.jello.editor.property.modifier.Space;
import com.codeshaper.jello.engine.Color;

@CreateAssetEntry(fileName = "material", location = "Material")
public class Material extends SerializedJelloObject {

	public Shader shader = null;
	@Space
	public Texture diffuseTexture = null;
	public Color diffuseColor = Color.white;
	
	public Material(Path file) {
		super(file);
	}
	
	public Material() {
		super(null);
	}
}
