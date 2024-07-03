package com.codeshaper.jello.engine.asset;

import java.io.File;

import com.codeshaper.jello.engine.GameObject;

public class Prefab extends Asset {

	public Prefab(File file) {
		super(file);
	}
	
	public GameObject createInstance() {
		return null; // TODO
	}
}
