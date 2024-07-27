package com.codeshaper.jello.engine.asset;

import com.codeshaper.jello.editor.property.modifier.CreateAssetEntry;
import com.codeshaper.jello.engine.AssetLocation;

@CreateAssetEntry(fileName = "terrainSettings", location = "Terrain Settings")
public class TerrainSettings extends SerializedJelloObject {
	
	public TerrainSettings(AssetLocation location) {
		super(location);
	}

	public class TreeInfo {
		
	}
}
