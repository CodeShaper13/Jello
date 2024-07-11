package com.codeshaper.jello.engine.asset;

import java.nio.file.Path;

import com.codeshaper.jello.editor.property.modifier.CreateAssetEntry;

@CreateAssetEntry(fileName = "material", location = "Material")
public class Material extends SerializedJelloObject {

	public Material(Path file) {
		super(file);
	}
}
