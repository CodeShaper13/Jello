package com.codeshaper.jello.engine.asset;

import com.codeshaper.jello.engine.AssetLocation;
import com.codeshaper.jello.engine.database.AssetDatabase;

/**
 * Returned by {@link AssetDatabase#getAsset(Path)} and
 * {@link AssetDatabase#getAsset(String)} when as Asset in the project has a
 * file type without an Asset class associated with that file type. A
 * {@link GenericAsset} has no fields, methods or functionality of any type and
 * is essentially a placeholder to stop null pointer exceptions.
 */
public final class GenericAsset extends Asset {

	public GenericAsset(AssetLocation location) {
		super(location);
	}
}
