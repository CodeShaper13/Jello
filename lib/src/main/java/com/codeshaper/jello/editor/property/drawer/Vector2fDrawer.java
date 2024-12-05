package com.codeshaper.jello.editor.property.drawer;

import org.joml.Vector2f;

@FieldDrawerType(Vector2f.class)
public class Vector2fDrawer extends InlineClassDrawer {

	public Vector2fDrawer() {
		super("x", "y");
	}

	@Override
	public Object createDefaultInstance() {
		return new Vector2f(0, 0);
	}
}
