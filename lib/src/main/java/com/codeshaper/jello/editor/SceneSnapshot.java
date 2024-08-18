package com.codeshaper.jello.editor;

import com.codeshaper.jello.engine.Scene;
import com.codeshaper.jello.engine.SceneManager;
import com.codeshaper.jello.engine.database.Serializer;
import com.google.gson.JsonElement;

public class SceneSnapshot {

	private JsonElement[] scenes;

	/**
	 * Creates a snapshot of the {@link SceneManager} that can be restored later.
	 * 
	 * @param sceneManager
	 */
	public SceneSnapshot(SceneManager sceneManager) {
		this.scenes = new JsonElement[sceneManager.getLoadedSceneCount()];

		Serializer serialize = JelloEditor.instance.assetDatabase.serializer;

		for (int i = 0; i < sceneManager.getLoadedSceneCount(); i++) {
			Scene scene = sceneManager.getLoadedScene(i);
			scenes[i] = serialize.serialize(scene);
		}
	}

	/**
	 * Restores the snapshot to a {@link SceneManager}.
	 * 
	 * @param sceneManager
	 */
	public void restore(SceneManager sceneManager) {
		Serializer serialize = JelloEditor.instance.assetDatabase.serializer;

		sceneManager.unloadAllScenes();
		for (JsonElement element : this.scenes) {
			Scene scene = serialize.deserialize(element, Scene.class);

			if (scene != null) {
				scene.onDeserialize();
				
				sceneManager.loadScene(scene);
			}
		}
	}
}
