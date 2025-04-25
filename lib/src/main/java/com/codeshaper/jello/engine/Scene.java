package com.codeshaper.jello.engine;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import com.codeshaper.jello.editor.GuiLayoutBuilder;
import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.inspector.Editor;
import com.codeshaper.jello.editor.property.modifier.CreateAssetEntry;
import com.codeshaper.jello.engine.asset.SerializedJelloObject;

@CreateAssetEntry(fileName = "scene", location = "Scene")
public final class Scene extends SerializedJelloObject {

	List<GameObject> rootGameObjects;

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
		
		for(GameObject obj : this.rootGameObjects) {
			this.recursivelySetupObject(obj);
		}
	}

	@Override
	public Editor<?> getEditor(JPanel panel) {
		return new CustomEditor(this, panel);
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
		gameObject.scene = this;
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
	
	public int getIndexOfRootGameObject(GameObject obj) {
		for(int i = 0; i < this.rootGameObjects.size(); i++) {
			if(this.rootGameObjects.get(i) == obj) {
				return i;
			}
		}
		
		return -1;
	}
	
	public GameObject getGameObject(String path) {
		String[] names = path.split("/");
		if(names.length == 0) {
			return null;
		}
		
		GameObject result = null;
		for(GameObject obj : this.rootGameObjects) {
			if(obj.getName().equals(names[0])) {
				result = obj;
				break;
			}
		}
		
		if(names.length == 1) { // Path has no children.
			return result;
		}
		
		for(int i = 1; i < names.length; i++) {
			GameObject child = result.getChild(names[i]);
			if(child == null) {
				return null;
			} else {
				result = child;
			}
		}
		
		return result;
	}

	/**
	 * Gets an iterator of all root GameObjects in the Scene.
	 * 
	 * @return
	 */
	public Iterable<GameObject> getRootGameObjects() {
		return this.rootGameObjects;
	}
	
	void recursivelySetupObject(GameObject gameObject) {
		gameObject.scene = this;
		for(JelloComponent component : gameObject.getAllComponents()) {
			component.owner = gameObject;
		}
		
		for(GameObject child : gameObject.getChildren()) {
			child.parent = gameObject;
			this.recursivelySetupObject(child);
		}
	}

	private class CustomEditor extends SerializedJelloObjectEditor<Scene> {

		public CustomEditor(Scene target, JPanel panel) {
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
