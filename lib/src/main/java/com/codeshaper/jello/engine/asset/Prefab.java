package com.codeshaper.jello.engine.asset;

import java.nio.file.Path;

import com.codeshaper.jello.engine.GameObject;

public class Prefab extends SerializedJelloObject {

	public Prefab(Path file) {
		super(file);
	}
	
	public GameObject createInstance() {
		return null; // TODO
	}
}
