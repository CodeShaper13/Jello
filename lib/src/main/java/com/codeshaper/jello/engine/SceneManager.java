package com.codeshaper.jello.engine;

import java.util.ArrayList;
import java.util.List;

public class SceneManager {

	protected List<Scene> loadedScenes;

	protected SceneManager() {
		this.loadedScenes = new ArrayList<Scene>();
	}

	/**
	 * Loads a Scene. If the Scene is already loaded, nothing happens.
	 * 
	 * @param scene the Scene to load.
	 * @return {code true} if the Scene was loaded.
	 * @throws IllegalArgumentException if scene is {@code null}
	 */
	public boolean loadScene(Scene scene) {
		if (scene == null) {
			throw new IllegalArgumentException("scene may not be null");
		}

		if (this.isSceneLoaded(scene)) {
			return false;
		}

		this.loadedScenes.add(scene);

		if (Application.isPlaying()) {
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
	 * @throws IllegalArgumentException if scene is {@code null}
	 */
	public boolean unloadScene(Scene scene) {
		if (scene == null) {
			throw new IllegalArgumentException("scene may not be null");
		}

		if (!this.isSceneLoaded(scene)) {
			return false;
		}

		if (Application.isPlaying()) {
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

	/**
	 * Gets a loaded Scene with a specific name. If there are multiple Scene's with
	 * the same name, the first one is returned. If not Scene is loaded with that
	 * name, {@code null} is returned.
	 * 
	 * @param sceneName the name of the scene to get
	 * @return a Scene with the specified name
	 * @throws IllegalArgumentException if sceneName is {@code null}
	 */
	public Scene getScene(String sceneName) {
		if (sceneName == null) {
			throw new IllegalArgumentException("sceneName may not be null");
		}

		for (Scene scene : this.loadedScenes) {
			if (scene.getAssetName().equals(sceneName)) {
				return scene;
			}
		}

		return null;
	}

	/**
	 * Gets the loaded Scene at a specific index. A Scene's index is it's order in
	 * the hierarchy If the index is out of bounds, {@code null} is returned.
	 * 
	 * @param index the index of the scene to get
	 * @return the Scene at the specified index
	 */
	public Scene getScene(int index) {
		if (index < 0 || index >= this.loadedScenes.size()) {
			return null;
		}

		return this.loadedScenes.get(index);
	}

	/**
	 * Gets the index of the specified scene. If the Scene is not loaded, {@code -1}
	 * is returned.
	 * 
	 * @param scene the Scene to get the index of
	 * @return the index of the specified scene, or {@code -1} if it is not loaded
	 * @throws IllegalArgumentException if scene is {@code null}
	 */
	public int getIndexOf(Scene scene) {
		this.loadedScenes.indexOf(scene);
		for (int i = 0; i < this.loadedScenes.size(); i++) {
			if (scene.equals(this.loadedScenes.get(i))) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Gets the number of loaded Scenes.
	 * 
	 * @return the number of loaded Scenes.
	 */
	public int getSceneCount() {
		return this.loadedScenes.size();
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
