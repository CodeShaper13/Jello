package com.codeshaper.jello.editor.inspector;

import javax.swing.JPanel;

import com.codeshaper.jello.editor.GuiLayoutBuilder;
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
	protected final GuiLayoutBuilder builder;
	
	/**
	 * Creates a new Editor.
	 * 
	 * All heavy lifting and memory allocations should be performed here.
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
		this.builder = new GuiLayoutBuilder();
	}
	
	public final void draw() {
		this.onDraw();
	}
	
	/**
	 * 
	 * @param isInitialDraw
	 */
	protected void onDraw() {

	}

	/**
	 * Called when the Editor goes away when the Inspector prepares to show a new
	 * Editor. Any cleanup should be performed here, like closing files or freeing
	 * native resources.
	 */
	public void onCleanup() {
	}
}
