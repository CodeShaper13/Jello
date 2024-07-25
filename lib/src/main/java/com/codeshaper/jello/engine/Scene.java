package com.codeshaper.jello.engine;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.codeshaper.jello.editor.GuiLayoutBuilder;
import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.inspector.Editor;
import com.codeshaper.jello.editor.property.modifier.CreateAssetEntry;
import com.codeshaper.jello.engine.asset.SerializedJelloObject;
import com.codeshaper.jello.engine.component.JelloComponent;

@CreateAssetEntry(fileName = "scene", location = "Scene")
public class Scene extends SerializedJelloObject {

	private List<GameObject> rootGameObjects;

	public Scene(Path path) {
		super(path);

		this.rootGameObjects = new ArrayList<GameObject>();
	}
	
	@Override
	public void onDeserialize() {
		super.onDeserialize();
		
		for(GameObject obj : this.rootGameObjects) {
			this.recursivelySetupObject(obj);
		}
	}

	@Override
	public Editor<?> getInspectorDrawer() {
		return new CustomEditor(this);
	}

	/**
	 * Moves a @{@link GameObject} to this Scene. If the GameObject is already in
	 * this Scene, nothing happens.
	 * 
	 * @param gameObject the GameObject to move.
	 */
	public void moveGameObjectTo(GameObject gameObject) {
		if (gameObject == null) {
			return;
		}

		Scene previousScene = gameObject.getScene();

		if (previousScene == this) {
			return; // gameObject is already in this scene.
		}

		if (previousScene != null) {
			previousScene.rootGameObjects.remove(gameObject);
		}

		this.rootGameObjects.add(gameObject);
	}

	/**
	 * Gets the number of root {@link GameObjects} in the Scene.
	 * 
	 * @return the number of GameObjects.
	 */
	public int getRootGameObjectCount() {
		return this.rootGameObjects.size();
	}

	public GameObject getRootGameObject(int index) {
		return this.rootGameObjects.get(index);
	}

	/**
	 * Gets an iterator of all root GameObjects in the Scene.
	 * 
	 * @return
	 */
	public Iterable<GameObject> getRootGameObjects() {
		return this.rootGameObjects;
	}
	
	void add(GameObject gameObject) {
		this.rootGameObjects.add(gameObject);
	}
	
	void remove(GameObject gameObject) {
		this.rootGameObjects.remove(gameObject);
	}
	
	private void recursivelySetupObject(GameObject gameObject) {
		gameObject.scene = this;
		for(JelloComponent component : gameObject.getAllComponents()) {
			component.gameObject = gameObject;
		}
		
		for(GameObject child : gameObject.getChildren()) {
			this.recursivelySetupObject(child);
		}
	}

	private class CustomEditor extends SerializedJelloObjectEditor<Scene> {

		public CustomEditor(Scene target) {
			super(target);
		}

		@Override
		public void drawAsset(GuiLayoutBuilder drawer) {
			drawer.button("Open Scene", null, () -> {
				JelloEditor.instance.sceneManager.loadScene(target);
			});
		}
	}
}
