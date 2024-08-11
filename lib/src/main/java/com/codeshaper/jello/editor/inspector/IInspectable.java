package com.codeshaper.jello.editor.inspector;

import javax.swing.JPanel;

/**
 * An interface for Objects that can be looked at and edited in the inspector.
 */
public interface IInspectable {
	
	public Editor<?> getInspectorDrawer(JPanel panel);
}
