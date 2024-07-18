package com.codeshaper.jello.editor.event;

import java.util.EventListener;

public interface ProjectReloadListener extends EventListener {

	/**
	 * 
	 * @param phase
	 */
	void onProjectReload(Phase phase);

	public enum Phase {
		/**
		 * Events with this phase are fired before the project is rebuilt.
		 */
		PRE_REBUILD,
		/**
		 * Events with this phase are fired immediately after the project is rebuilt.
		 */
		REBUILD,
		/**
		 * Events with this phase are fired after all listeners have been invoked with
		 * the {@link Phase#AFTER}. Use this to have logic that depends on another
		 * listener having already run.
		 */
		POST_REBUILD,
	}
}
