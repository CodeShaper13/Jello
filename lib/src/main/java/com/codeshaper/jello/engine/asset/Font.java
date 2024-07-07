package com.codeshaper.jello.engine.asset;

import java.io.File;

import com.codeshaper.jello.engine.AssetFileExtension;

@AssetFileExtension(".ttf")
public class Font extends Asset {

	public Font(File file) {
		super(file);
	}
}
