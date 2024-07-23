package com.codeshaper.jello.engine;

import java.util.ArrayList;

import com.codeshaper.jello.engine.asset.Scene;

public class SceneManager {

	protected ArrayList<Scene> loadedScenes;

	protected SceneManager() {
		this.loadedScenes = new ArrayList<Scene>();
	}

	/**
	 * Loads a Scene. If the Scene is already loaded, nothing happens.
	 * 
	 * @param scene the Scene to load.
	 * @return {code true} if the Scene was loaded.
	 */
	public boolean loadScene(Scene scene) {
		if (this.isSceneLoaded(scene)) {
			return false;
		}

		this.loadedScenes.add(scene);
		return true;
	}

	/**
	 * Unloads a Scene. If the Scene is not loaded, nothing happens.
	 * 
	 * @param scene the Scene to unload.
	 * @return {code true} if the Scene was unloaded.
	 */
	public boolean unloadScene(Scene scene) {
		if (!this.isSceneLoaded(scene)) {
			return false;
		}

		this.loadedScenes.remove(scene);
		return true;
	}

	/**
	 * Unloads all Scenes.
	 */
	public void unloadAllScenes() {
		for (int i = this.loadedScenes.size() - 1; i >= 0; i--) {
			this.unloadScene(this.loadedScenes.get(i));
		}
	}

	/**
	 * Checks if a Scene is loaded.
	 * 
	 * @param scene the Scene to check.
	 * @return {@code true} if the Scene is loaded.
	 */
	public boolean isSceneLoaded(Scene scene) {
		for (Scene s : this.loadedScenes) {
			if (s == scene) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Gets the number of loaded {@link Scene}s.
	 * 
	 * @return the number of loaded {@link Scene}s.
	 */
	public int getLoadedSceneCount() {
		return this.loadedScenes.size();
	}

	public Scene getLoadedScene(int index) {
		return this.loadedScenes.get(index);
	}

	public Iterable<Scene> getScenes() {
		return this.loadedScenes;
	}
}
