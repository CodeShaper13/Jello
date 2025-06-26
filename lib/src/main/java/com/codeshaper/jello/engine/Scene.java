package com.codeshaper.jello.engine;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.gui.GuiLayoutBuilder;
import com.codeshaper.jello.editor.inspector.Editor;
import com.codeshaper.jello.editor.property.modifier.CreateAssetEntry;
import com.codeshaper.jello.engine.asset.SerializedJelloObject;

/**
 * A Scene is nothing more than a collection of {@link GameObject}s. The
 * GameObjects are stored in a hierarchal way. There can be any number of
 * GameObjects at the base of the hierarchy. These GameObjects are referred to
 * as "root game objects".
 * <p>
 * Scenes can be loaded and unloaded at any time, and there can be an infinite
 * number of Scenes loaded at once.
 * <p>
 * The same Scene can not have multiple instances of itself loaded at the same
 * time.
 */
@CreateAssetEntry(fileName = "scene", location = "Scene")
public final class Scene extends SerializedJelloObject {

	/**
	 * The GameObjects at the base of the Scene hierarchy. While this collection
	 * will never be null, it could be empty.
	 */
	final List<GameObject> rootGameObjects;

	public Scene(AssetLocation location) {
		super(location);

		this.rootGameObjects = new ArrayList<GameObject>();
	}

	@Override
	public String toString() {
		return "Scene [name=" + this.getAssetName() + "]";
	}

	@Override
	public void onDeserialize() {
		super.onDeserialize();

		for (GameObject obj : this.rootGameObjects) {
			this.recursivelySetupObject(obj);
		}
	}

	@Override
	public Editor<?> getEditor(JPanel panel) {
		return new SceneEditor(this, panel);
	}

	/**
	 * Moves a @{@link GameObject} to this Scene. If the GameObject is already in
	 * this Scene, nothing happens.
	 * 
	 * @param gameObject the GameObject to move
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
			// Remove the GameObject from it's old Scene.
			if(gameObject.isRoot()) {
				previousScene.rootGameObjects.remove(gameObject);
			} else {
				gameObject.setParent(null);
			}
		}

		this.rootGameObjects.add(gameObject);
		
		this.recursivelySetScene(gameObject);
	}

	/**
	 * Gets the number of root {@link GameObjects} in the Scene. A root GameObject
	 * is any GameObject without a parent, and thus is at the top of the hierarchy.
	 * 
	 * @return the number of GameObjects.
	 */
	public int getRootGameObjectCount() {
		return this.rootGameObjects.size();
	}

	/**
	 * Gets a root {@link GameObject} from the Scene based on it's index. If the
	 * index is out of bounds, null is returned.
	 *
	 * @param index the index of the GameObject
	 * @return the GameObject at the passed index, or null if the index is out of
	 *         bounds.
	 */
	public GameObject getRootGameObject(int index) {
		if (index < 0 || index >= this.rootGameObjects.size()) {
			return null;
		}

		return this.rootGameObjects.get(index);
	}

	/**
	 * Gets the index of a root {@link GameObject} in this Scene. If the passed
	 * GameObject is not a root GameObject to this Scene, or {@code null} -1 is
	 * returned.
	 * 
	 * @param gameObject
	 * @return the index of the GameObject, or -1 if it is not a root GameObject.
	 * 
	 * @see GameObject#getIndexOf()
	 */
	public int getIndexOfRootGameObject(GameObject gameObject) {
		if (gameObject == null) {
			return -1;
		}

		this.rootGameObjects.indexOf(gameObject);
		for (int i = 0; i < this.rootGameObjects.size(); i++) {
			if (this.rootGameObjects.get(i) == gameObject) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Get's a {@link GameObject} from a string path.
	 * 
	 * @param path
	 * @return
	 * 
	 * @see GameObject#getPath(boolean)
	 */
	public GameObject getGameObject(String path) {
		String[] names = path.split("/");
		if (names.length == 0) {
			return null;
		}

		GameObject result = null;
		for (GameObject obj : this.rootGameObjects) {
			if (obj.getName().equals(names[0])) {
				result = obj;
				break;
			}
		}

		if (names.length == 1) { // Path has no children.
			return result;
		}

		for (int i = 1; i < names.length; i++) {
			GameObject child = result.getChild(names[i]);
			if (child == null) {
				return null;
			} else {
				result = child;
			}
		}

		return result;
	}

	/**
	 * Gets an iterator that will go through all root {@link GameO} GameObjects in the Scene. If
	 * you plan to iterate through all of the GameObject in the Scene and possibly
	 * add or remove any, use {@link Scene#getRootGameObjectCount()} and
	 * {@link Scene#getRootGameObject(int)}.
	 * 
	 * @return an iterator containing all of the root GameObjects
	 */
	public Iterable<GameObject> getRootGameObjects() {
		return this.rootGameObjects;
	}

	void recursivelySetupObject(GameObject gameObject) {
		gameObject.scene = this;
		for (JelloComponent component : gameObject.getAllComponents()) {
			component.owner = gameObject;
		}

		for (GameObject child : gameObject.getChildren()) {
			child.parent = gameObject;
			this.recursivelySetupObject(child);
		}
	}
	
	// Used by Scene#moveGameObjectTo
	private void recursivelySetScene(GameObject obj) {
		obj.scene = this;
		for(GameObject child : obj.getChildren()) {
			this.recursivelySetScene(child);
		}
	}

	private class SceneEditor extends SerializedJelloObjectEditor<Scene> {

		public SceneEditor(Scene target, JPanel panel) {
			super(target, panel);
		}

		@Override
		public void drawAsset(GuiLayoutBuilder drawer) {
			drawer.button("Open Scene", null, () -> {
				JelloEditor.instance.sceneManager.loadScene(target);
			});
		}
	}
}
