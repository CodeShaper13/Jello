package com.codeshaper.jello.editor.property.drawer;

import org.joml.Vector4i;

@FieldDrawerType(Vector4i.class)
public class Vector4iDrawer extends InlineClassDrawer {

	public Vector4iDrawer() {
		super("w", "x", "y", "z");
	}

	@Override
	public Object createDefaultInstance() {
		return new Vector4i(0, 0, 0, 0);
	}
}
