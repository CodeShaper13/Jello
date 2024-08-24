package com.codeshaper.jello.editor.inspector;

import javax.swing.JPanel;

import com.codeshaper.jello.editor.window.InspectorWindow;
import com.codeshaper.jello.engine.JelloObject;

/**
 * Any object that can be shown in the Inspector must provide an Editor to do
 * the drawing.
 */
public abstract class Editor<T extends JelloObject> {

	/**
	 * The object this Editor is for.
	 */
	protected final T target;
	protected final JPanel panel;

	/**
	 * 
	 * @param target
	 * @param panel
	 */
	public Editor(T target, JPanel panel) {
		if (target == null) {
			throw new IllegalArgumentException("target may not be null.");
		}
		this.target = target;
		this.panel = panel;
	}

	/**
	 * Called when the Editor is refreshed. An Editor refresh can occur from the
	 * project being reloaded, the "Refresh" button being clicked, or a call to
	 * {@link InspectorWindow#refresh()}
	 */
	public void onRefresh() { }

	/**
	 * Called when the Editor goes away when the Inspector prepares to show a new
	 * Editor. Any cleanup should be performed here.
	 */
	public void onCleanup() { }
}
