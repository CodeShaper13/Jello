package com.codeshaper.jello.editor.inspector;

import javax.swing.JPanel;
/**
 * Any object that can be shown in the editor must provide an Editor to do the
 * drawing.
 */
public class Editor<T extends IInspectable> {

	protected final T target;
	protected final JPanel panel;

	public Editor(T target, JPanel panel) {
		this.target = target;
		this.panel = panel;
	}
	
	public void draw() { }
		
	public void refresh() { }

	public void cleanup() { }
}
