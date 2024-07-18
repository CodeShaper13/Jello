package com.codeshaper.jello.editor.event;

import java.util.EventListener;

import com.codeshaper.jello.engine.asset.Scene;

/**
 * The listener interface for when the Application is started within the Editor.
 */
public interface SceneChangeListener extends EventListener {

	/**
	 * 
	 * @param oldScene the previously loaded scene. May be null.
	 * @param newScene the scene that was just loaded. May be null.
	 */
	void onSceneChange(Scene oldScene, Scene newScene);
}
