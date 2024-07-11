package com.codeshaper.jello.engine.asset;

import java.nio.file.Path;

import com.codeshaper.jello.editor.property.modifier.CreateAssetEntry;

@CreateAssetEntry(fileName = "tile", location = "Tile")
public class Tile extends SerializedJelloObject {

	public Tile(Path file) {
		super(file);
	}

	public Texture texture;
}
