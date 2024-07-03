package com.codeshaper.jello.engine;

import com.codeshaper.jello.engine.asset.Scene;

public interface ISceneProvider {

	/**
	 * Provides an Iterable that will return every scene that the implementing
	 * object provides.
	 */
	Iterable<Scene> getScenes();
}
