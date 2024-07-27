package com.codeshaper.jello.engine.asset;

import com.codeshaper.jello.editor.property.modifier.CreateAssetEntry;
import com.codeshaper.jello.engine.AssetLocation;

@CreateAssetEntry(fileName = "tile", location = "Tile")
public class Tile extends SerializedJelloObject {

	public Tile(AssetLocation location) {
		super(location);
	}

	public Texture texture;
}
