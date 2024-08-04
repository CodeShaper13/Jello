package com.codeshaper.jello.editor.property.drawer;

import org.joml.Vector4f;

@FieldDrawerType(Vector4f.class)
public class Vector4fDrawer extends InlineClassDrawer {

	public Vector4fDrawer() {
		super("w", "x", "y", "z");
	}
}
