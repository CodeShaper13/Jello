package com.codeshaper.jello.editor;

import java.util.ArrayList;
import java.util.List;

import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.Scene;
import com.codeshaper.jello.engine.SceneManager;
import com.codeshaper.jello.engine.database.AssetDatabase;
import com.codeshaper.jello.engine.database.Serializer;
import com.google.gson.JsonElement;

public class SceneManagerSnapshot {

	private List<SceneSnapshot> scenes;

	/**
	 * Creates a snapshot of the {@link SceneManager} that can be restored later.
	 * 
	 * @param sceneManager
	 */
	public SceneManagerSnapshot(SceneManager sceneManager) {
		this.scenes = new ArrayList<SceneSnapshot>(sceneManager.getLoadedSceneCount());

		for (int i = 0; i < sceneManager.getLoadedSceneCount(); i++) {
			scenes.add(new SceneSnapshot(sceneManager.getLoadedScene(i)));
		}
	}

	/**
	 * Restores the snapshot to a {@link SceneManager}.
	 * 
	 * @param sceneManager the Scene Manager to restore the snapshot to.
	 */
	public void restore(SceneManager sceneManager) {
		Serializer serialize = AssetDatabase.getInstance().serializer;

		sceneManager.unloadAllScenes();

		for (SceneSnapshot data : this.scenes) {
			Scene scene = data.scene;

			// Remove all GameObjects from the scene.
			for (int i = scene.getRootGameObjectCount() - 1; i >= 0; i--) {
				scene.getRootGameObject(i).destroy();
			}

			// Add all GameObjects that existed prior to play mode.
			Scene originalScene = serialize.deserialize(data.json, Scene.class);
			for (int i = originalScene.getRootGameObjectCount() - 1; i >= 0; i--) {
				GameObject obj = originalScene.getRootGameObject(i);
				scene.moveGameObjectTo(obj);
			}

			sceneManager.loadScene(data.scene);
			scene.onDeserialize();
		}
	}

	private class SceneSnapshot {

		/**
		 * The Scene that this snapshot came from.
		 */
		public final Scene scene;
		/**
		 * Json data representing the scene.
		 */
		public final JsonElement json;

		/**
		 * Creates a snapshot of a scene.
		 * 
		 * @param scene the scene to create the snapshot from
		 * @throws IllegalArgumentException if {@code scene} is null
		 */
		public SceneSnapshot(Scene scene) {
			if (scene == null) {
				throw new IllegalArgumentException("scene may not be null");
			}
			this.scene = scene;
			this.json = AssetDatabase.getInstance().serializer.serializeToJsonElement(scene);
		}
	}
}
