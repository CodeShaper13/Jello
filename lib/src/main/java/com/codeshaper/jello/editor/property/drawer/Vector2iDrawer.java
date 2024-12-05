package com.codeshaper.jello.editor.property.drawer;

import org.joml.Vector2i;

@FieldDrawerType(Vector2i.class)
public class Vector2iDrawer extends InlineClassDrawer {

	public Vector2iDrawer() {
		super("x", "y");
	}

	@Override
	public Object createDefaultInstance() {
		return new Vector2i(0, 0);
	}
}
