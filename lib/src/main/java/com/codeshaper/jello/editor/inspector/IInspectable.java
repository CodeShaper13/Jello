package com.codeshaper.jello.editor.inspector;

/**
 * An interface for Objects that can be looked at and edited in the inspector.
 */
public interface IInspectable {
	
	public Editor<?> getInspectorDrawer();
}
