package com.codeshaper.jello.engine.asset;

import com.codeshaper.jello.engine.AssetFileExtension;
import com.codeshaper.jello.engine.AssetLocation;

@AssetFileExtension(".ttf")
public class Font extends Asset {

	public Font(AssetLocation location) {
		super(location);
	}
}
