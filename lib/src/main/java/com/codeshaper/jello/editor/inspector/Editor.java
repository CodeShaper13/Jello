package com.codeshaper.jello.editor.inspector;

import javax.swing.JPanel;
/**
 * Any object that can be shown in the editor must provide an Editor to do the
 * drawing.
 */
public class Editor<T extends IInspectable> {

	protected final T target;

	public Editor(T target) {
		this.target = target;
	}
	
	public void draw(JPanel panel) { }
		
	public void refresh() { }

	public void cleanup() { }
}
