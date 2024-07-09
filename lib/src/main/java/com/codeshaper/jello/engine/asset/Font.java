package com.codeshaper.jello.engine.asset;

import java.nio.file.Path;

import com.codeshaper.jello.engine.AssetFileExtension;

@AssetFileExtension(".ttf")
public class Font extends Asset {

	public Font(Path file) {
		super(file);
	}
}
