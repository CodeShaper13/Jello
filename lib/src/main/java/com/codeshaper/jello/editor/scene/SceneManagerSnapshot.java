package com.codeshaper.jello.editor.scene;

import java.util.ArrayList;
import java.util.List;

import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.JelloComponent;
import com.codeshaper.jello.engine.Scene;
import com.codeshaper.jello.engine.SceneManager;
import com.codeshaper.jello.engine.database.AssetDatabase;
import com.codeshaper.jello.engine.database.Serializer;
import com.google.gson.JsonElement;

/**
 * Provides a way to create a snapshot that captures the state of all loaded
 * {@link Scene}s in a {@link SceneManager}. This state can later be restored to
 * a SceneManager with {@link SceneManagerSnapshot#restore(SceneManager)}
 * <p>
 * Snapshots include everything in a Scenes, the {@link GameObject}s, their
 * {@link JelloComponent}s, the Component's state, ect.
 * <p>
 * Snapshots are not tied to a specific Scene Manager, meaning a snapshot could
 * be created from one Scene Manager and restored to a different one.
 */
public class SceneManagerSnapshot {

	private List<SceneSnapshot> scenes;

	/**
	 * Creates a snapshot of the {@link SceneManager}.
	 * 
	 * @param sceneManager the Scene Manager to create a snapshot of
	 */
	public SceneManagerSnapshot(SceneManager sceneManager) {
		this.scenes = new ArrayList<SceneSnapshot>(sceneManager.getSceneCount());

		for (int i = 0; i < sceneManager.getSceneCount(); i++) {
			scenes.add(new SceneSnapshot(sceneManager.getScene(i)));
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

			// Add all GameObjects that exist in the snapshot.
			Scene originalScene = serialize.deserialize(data.json, Scene.class);
			for (int i = 0; i < originalScene.getRootGameObjectCount(); i++) {
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
