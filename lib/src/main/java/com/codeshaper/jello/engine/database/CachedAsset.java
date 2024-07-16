package com.codeshaper.jello.engine.database;

import java.nio.file.Path;

import com.codeshaper.jello.engine.asset.Asset;
import com.codeshaper.jello.engine.asset.Material;
import com.codeshaper.jello.engine.asset.SerializedJelloObject;

public class CachedAsset {

	private Path path;
	private Class<? extends Asset> providingClass;
	/**
	 * The instance of the Asset. If the Asset is not loaded, this is null.
	 */
	public Asset instance;

	public CachedAsset(Path path, Class<? extends Asset> providingClass) {
		this.path = path;
		this.providingClass = providingClass;
	}

	/**
	 * Gets relative path to the file providing the Asset, starting at the /assets
	 * folder, or /builtin if it's a builtin Asset.
	 * 
	 * @return a relative path to the Asset.
	 */
	public Path getPath() {
		return this.path;
	}
	
	public void setPath(Path path) {
		this.path = path;
		if(this.isLoaded()) {
			this.instance.file = path;
		}
	}

	/**
	 * Gets the class that provides the implementation of the Asset in code. For
	 * Assets that extends @link {@link SerializedJelloObject} (e.g. Material), the
	 * exact class is returned (e.g. {@link Material}.
	 * 
	 * @return
	 */
	public Class<? extends Asset> getProvidingClass() {
		return this.providingClass;
	}

	/**
	 * Checks if the Asset has been loaded.
	 * 
	 * @return
	 */
	public boolean isLoaded() {
		return this.instance != null;
	}
}
