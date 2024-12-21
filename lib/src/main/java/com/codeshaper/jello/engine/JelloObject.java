package com.codeshaper.jello.engine;

import javax.swing.JPanel;

import com.codeshaper.jello.editor.inspector.Editor;

/**
 * Base class for all Objects that Jello can view with the Inspector.
 */
public abstract class JelloObject {

	private boolean isDestroyed = false;

	/**
	 * Destroys the {@link JelloObject}.
	 */
	public void destroy() {
		this.isDestroyed = true;
	}

	/**
	 * Checks if the object has been destroyed. Before accessing any JelloObject,
	 * {@link JelloObject#isDestroyed()} should be used to check if it still exists.
	 * If not, the reference should be set to null so the garbage collector can
	 * remove it.
	 * 
	 * @return {@code true} if the object has been destroyed
	 */
	public boolean isDestroyed() {
		return this.isDestroyed;
	}

	public abstract String getPersistencePath();

	/**
	 * Gets an {@link Editor} to use when drawing the JelloObject in the inspector.
	 * 
	 * @param panel
	 * @return
	 */
	public abstract Editor<?> getInspectorDrawer(JPanel panel);
}
