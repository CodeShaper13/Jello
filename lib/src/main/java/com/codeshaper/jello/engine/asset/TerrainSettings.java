package com.codeshaper.jello.engine.asset;

import java.nio.file.Path;

import com.codeshaper.jello.editor.property.modifier.CreateAssetEntry;

@CreateAssetEntry(fileName = "terrainSettings", location = "Terrain Settings")
public class TerrainSettings extends SerializedJelloObject {
	
	public TerrainSettings(Path file) {
		super(file);
	}

	public class TreeInfo {
		
	}
}
