package com.codeshaper.jello.editor.event;

import java.util.EventListener;

import com.codeshaper.jello.engine.Scene;

/**
 * The listener interface for when the Application is started within the Editor.
 */
public interface SceneChangeListener extends EventListener {

	/**
	 * @param scene  the Scene that is changing.
	 * @param action what happened to the Scene.
	 */
	void onSceneChange(Scene scene, Action action);

	public enum Action {
		LOAD,
		UNLOAD,
	}
}
