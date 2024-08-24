package com.codeshaper.jello.engine;

import java.util.ArrayList;

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
		
		if(Application.isPlaying()) {
			for (int i = scene.getRootGameObjectCount() - 1; i >= 0; i--) {
				this.recursiveCallOnConstruct(scene.getRootGameObject(i));
			}
		}

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
		
		if(Application.isPlaying()) {
			// Destroy all GameObjects in the Scene.
			for (int i = scene.getRootGameObjectCount() - 1; i >= 0; i--) {
				scene.getRootGameObject(i).destroy();
			}
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
	
	public Scene getScene(String sceneName) {
		if(sceneName == null) {
			throw new IllegalArgumentException("sceneName may not be null");
		}
		
		for(Scene scene : this.loadedScenes) {
			if(scene.getAssetName().equals(sceneName)) {
				return scene;
			}
		}
		
		return null;
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

	void recursiveCallOnConstruct(GameObject parent) {
		for (int i = parent.getComponentCount() - 1; i >= 0; i--) {
			parent.getComponentAtIndex(i).invokeOnConstruct();
		}
		
		for (int i = parent.getChildCount() - 1; i >= 0; i--) {
			this.recursiveCallOnConstruct(parent.getChild(i));
		}
	}
}
