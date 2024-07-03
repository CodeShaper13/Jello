package com.codeshaper.jello.engine;

import java.io.File;
import java.util.Hashtable;
import java.util.List;

import com.codeshaper.jello.engine.asset.Asset;

public class AssetDatabase {
	
	private final Hashtable<File, Asset> assets;
	
	public AssetDatabase() {
		this.assets = new Hashtable<File, Asset>();
	}
	
	public <T extends Asset> T getAsset(String path) {
		return null;
	}

	public <T extends Asset> T getAsset(File file) {
		return null;
	}
	
	public <T extends Asset> List<T> getAssets(Class<T> type) {
		return null;
	}
}
