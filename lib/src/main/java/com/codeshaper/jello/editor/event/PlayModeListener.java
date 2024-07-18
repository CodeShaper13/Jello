package com.codeshaper.jello.editor.event;

import java.util.EventListener;

/**
 * The listener interface for when the Application is started within the Editor.
 */
public interface PlayModeListener extends EventListener {

	void onPlaymodeChange(State state);
	
	public enum State {	
		STARTED,
		PAUSED, // Not yet implemented
		STOPPED,	
	}
}
