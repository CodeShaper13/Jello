package com.codeshaper.jello.engine.asset;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.inspector.Editor;
import com.codeshaper.jello.editor.inspector.GuiDrawer;
import com.codeshaper.jello.editor.property.modifier.CreateAssetEntry;
import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.component.JelloComponent;

@CreateAssetEntry(fileName = "scene", location = "Scene")
public class Scene extends SerializedJelloObject {

	/**
	 * A list of all of the GameObjects in the scene.
	 */
	private List<GameObject> rootGameObjects;
	
	public Scene(Path assetFile) {
		super(assetFile);
		
		this.rootGameObjects = new ArrayList<GameObject>();
	}	
	
	@Override
	public Editor<?> getInspectorDrawer() {
		return new CustomEditor(this);
	}

	/**
	 * 
	 * @param <T>
	 * @param objectName The name of the GameObject.
	 * @return The newly created GameObject.
	 */
	public <T extends JelloComponent> GameObject instantiateGameObject(String objectName) {
		GameObject object = new GameObject(objectName);
		this.rootGameObjects.add(object);
		
		return object;
	}
	
	/**
	 * @return The name of the scene.
	 */
	public String getSceneName() {
		return FilenameUtils.removeExtension(this.file.getFileName().toString());
	}
	
	public Iterable<GameObject> getRootGameObjects() {
		return this.rootGameObjects;
	}
	
	private class CustomEditor extends SerializedJelloObjectEditor<Scene> {

		public CustomEditor(Scene target) {
			super(target);
		}

		@Override
		public void drawAsset(GuiDrawer drawer) {
			drawer.drawButton("Open Scene", null, () -> {
            	JelloEditor.instance.setScene(target);
            });
		}
	}
}
