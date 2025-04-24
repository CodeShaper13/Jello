package com.codeshaper.jello.engine;

import javax.swing.JPanel;

import com.codeshaper.jello.editor.inspector.Editor;

/**
 * Base class for all Objects that Jello can view with the Inspector.
 */
public abstract class JelloObject {

	private boolean isDestroyed = false;
	/**
	 * An integer that is treated as an array of boolean values, that keep track of
	 * the GameObject's state in the Editor.
	 * <p>
	 * Bit 0: Stores if the Object is expanded in the hierarchy.
	 * <P>
	 * the rest of the bits are unused.
	 */
	protected int editorState; // TODO make protected

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
	
	////////////////////////////////////////////////////
	// Methods intended to be used in the Editor only //
	////////////////////////////////////////////////////

	/**
	 * Checks if the Object is expanded in the Hierarchy.
	 * <p>
	 * This method is only intended to be used in the Editor, though calling it in
	 * builds is completely safe.
	 * 
	 * @return
	 */
	public boolean isExpandedInHierarchy() {
		return ((this.editorState >> 0) & 1) == 1;
	}

	/**
	 * Sets if the Object is expanded in the Editor or not.
	 * <p>
	 * This method is only intended to be used in the Editor, though calling it in
	 * builds is completely safe.
	 * 
	 * @param isExpanded
	 */
	public void setExpandedInHierarchy(boolean isExpanded) {
		if(isExpanded) {
			this.editorState |= (1 << 0);			
		} else {
			this.editorState &= ~(1 << 0);  
		}
	}
}
