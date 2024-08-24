package com.codeshaper.jello.engine;

import javax.swing.JPanel;

import com.codeshaper.jello.editor.inspector.Editor;

/**
 * Base class for all Objects that Jello can view with the Inspector.
 */
public abstract class JelloObject {

	public abstract String getPersistencePath();

	/**
	 * Gets an {@link Editor} to use when drawing the JelloObject in the inspector.
	 * 
	 * @param panel
	 * @return
	 */
	public abstract Editor<?> getInspectorDrawer(JPanel panel);
}
