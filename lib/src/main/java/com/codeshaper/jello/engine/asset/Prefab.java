package com.codeshaper.jello.engine.asset;

import com.codeshaper.jello.engine.AssetLocation;
import com.codeshaper.jello.engine.GameObject;

public class Prefab extends SerializedJelloObject {

	public Prefab(AssetLocation location) {
		super(location);
	}
	
	public GameObject createInstance() {
		return null; // TODO
	}
}
