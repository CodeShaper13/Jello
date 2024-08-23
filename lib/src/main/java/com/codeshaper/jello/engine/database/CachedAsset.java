package com.codeshaper.jello.engine.database;

import com.codeshaper.jello.engine.AssetLocation;
import com.codeshaper.jello.engine.asset.Asset;
import com.codeshaper.jello.engine.asset.Material;
import com.codeshaper.jello.engine.asset.SerializedJelloObject;

public class CachedAsset {

	/**
	 * The location of the Asset.
	 */
	public final AssetLocation location;
	/**
	 * The class that provides the implementation of the Asset in code. For Assets
	 * that extends @link {@link SerializedJelloObject} (e.g. {@link Material} the
	 * exact class is returned (e.g. {@link Material}.
	 * 
	 * @return the class providing the implementation of the Asset.
	 */
	public final Class<? extends Asset> providingClass;
	/**
	 * THe last time this Asset was loaded/reloaded.
	 */
	private long lastLoaded;
	/**
	 * The instance of the Asset. If the Asset is not loaded, this is {@code null}.
	 */
	private Asset instance;

	public CachedAsset(AssetLocation location, Class<? extends Asset> providingClass) {
		this.location = location;
		this.providingClass = providingClass;
	}

	/**
	 * Checks if the Asset is loaded.
	 * 
	 * @return {@code true} if the Asset is loaded.
	 */
	public boolean isLoaded() {
		return this.instance != null;
	}

	public Asset getInstance() {
		return this.instance;
	}

	public void setInstance(Asset instance) {
		this.instance = instance;
		if (instance != null) {
			this.lastLoaded = System.currentTimeMillis();
		}
	}

	/**
	 * Gets the class that provides the implementation of the Asset in code. For
	 * Assets that extends @link {@link SerializedJelloObject} (e.g. Material), the
	 * exact class is returned (e.g. {@link Material}.
	 * 
	 * @return the class providing the implementation of the Asset.
	 */
	public Class<? extends Asset> getProvidingClass() {
		return this.providingClass;
	}

	/**
	 * Checks if this is a builtin Asset.
	 * 
	 * @return {@link true} if this is a builtin Asset.
	 */
	public boolean isBuiltin() {
		return this.location.isBuiltin();
	}

	/**
	 * Checks if the file providing this Asset has been modified since it was
	 * loaded.
	 * 
	 * @return {@code true} if the Asset's file has been modified.
	 */
	public boolean hasFileBeenModified() {
		if (this.lastLoaded == 0) {
			return false;
		} else {
			return this.lastLoaded < this.location.getFile().lastModified();
		}
	}
}
