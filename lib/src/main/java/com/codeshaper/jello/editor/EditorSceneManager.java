package com.codeshaper.jello.editor;

import com.codeshaper.jello.editor.event.SceneChangeListener;
import com.codeshaper.jello.editor.event.SceneChangeListener.Action;
import com.codeshaper.jello.engine.AssetLocation;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.Scene;
import com.codeshaper.jello.engine.SceneManager;
import com.codeshaper.jello.engine.asset.Asset;

public class EditorSceneManager extends SceneManager {

	private final JelloEditor editor;

	public EditorSceneManager(JelloEditor editor) {
		this.editor = editor;
	}

	@Override
	public boolean loadScene(Scene scene) {
		if (super.loadScene(scene)) {
			this.editor.raiseEvent(SceneChangeListener.class, (listener) -> {
				listener.onSceneChange(scene, Action.LOAD);
			});
			return true;
		}
		return false;
	}

	@Override
	public boolean unloadScene(Scene scene) {
		if (super.unloadScene(scene)) {
			this.editor.raiseEvent(SceneChangeListener.class, (listener) -> {
				listener.onSceneChange(scene, Action.UNLOAD);
			});
			return true;
		}
		return false;
	}

	/**
	 * Saves a Scene to disk.
	 * 
	 * @param scene the Scene to save.
	 */
	public void saveScene(Scene scene) {
		this.editor.assetDatabase.saveAsset(scene);
	}

	/**
	 * Saves all open Scenes to disk.
	 */
	public void saveAllScenes() {
		for (Scene scene : this.getScenes()) {
			this.saveScene(scene);
		}
	}

	public void readOpenScenesFromPreferences() {
		int sceneCount = this.editor.properties.getInt("loadedSceneCount", 0);
		for (int i = 0; i < sceneCount; i++) {
			String stringPath = this.editor.properties.getString("loadedScene" + i, null);
			if (stringPath != null) {
				AssetLocation location = new AssetLocation(stringPath);
				if(this.editor.assetDatabase.exists(location)) {
					Asset asset = this.editor.assetDatabase.getAsset(location);
					if (asset != null && asset instanceof Scene) {
						this.loadScene((Scene) asset);
					}	
				} else {
					Debug.logWarning("Unable to open Scene at \"%s\"", location.toString());
				}
			}
		}
	}

	public void writeOpenScenesToPreferences() {
		int sceneCount = this.getLoadedSceneCount();
		this.editor.properties.setInt("loadedSceneCount", sceneCount);
		for (int i = 0; i < sceneCount; i++) {
			this.editor.properties.setString("loadedScene" + i, this.getLoadedScene(i).location.getRelativePath().toString());
		}
	}
}
