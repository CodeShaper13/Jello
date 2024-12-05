package com.codeshaper.jello.editor.property.drawer;

import org.joml.Vector3i;

@FieldDrawerType(Vector3i.class)
public class Vector3iDrawer extends InlineClassDrawer {

	public Vector3iDrawer() {
		super("x", "y", "z");
	}

	@Override
	public Object createDefaultInstance() {
		return new Vector3i(0, 0, 0);
	}
}
