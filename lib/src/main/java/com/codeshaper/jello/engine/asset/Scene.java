package com.codeshaper.jello.engine.asset;

import java.util.ArrayList;
import java.util.List;

import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.component.JelloComponent;

public class Scene extends Asset {

	/**
	 * A list of all of the GameObjects in the scene.
	 */
	private List<GameObject> rootGameObjects;
	
	public Scene() {
		super(null);
		
		this.rootGameObjects = new ArrayList<GameObject>();
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
	public String GetSceneName() {
		return "";
	}
	
	public Iterable<GameObject> getRootGameObjects() {
		return this.rootGameObjects;
	}
}
