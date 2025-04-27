package com.codeshaper.jello.engine.asset;

import java.util.Objects;

import com.codeshaper.jello.editor.property.modifier.CreateAssetEntry;
import com.codeshaper.jello.engine.AssetLocation;
import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.Scene;
import com.codeshaper.jello.engine.database.AssetDatabase;
import com.google.gson.JsonElement;

@CreateAssetEntry(fileName = "prefab", location = "Prefab")
public class Prefab extends SerializedJelloObject {

	private JsonElement prefabData;

	public Prefab(AssetLocation location) {
		super(location);
	}

	public void save(GameObject obj) {
		Objects.requireNonNull(obj, "gameObject must not be null");
		this.prefabData = AssetDatabase.getInstance().serializer.serializeToJsonElement(obj);
	}

	/**
	 * 
	 * @param parent
	 * @return
	 * @throws NullPointerException if {@code parent} is null
	 */
	public GameObject createInstance(GameObject parent) {
		Objects.requireNonNull(parent, "parent must not be null");
		return GameObject.fromJson(prefabData, parent);
	}

	/**
	 * @param scene
	 * @return
	 * @throws NullPointerException if {@code scene} is null
	 */
	public GameObject createInstance(Scene scene) {
		Objects.requireNonNull(scene, "scene must not be null");
		return GameObject.fromJson(prefabData, scene);
	}
}
