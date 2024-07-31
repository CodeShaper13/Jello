package com.codeshaper.jello.editor.property.drawer;

import org.joml.Vector3f;

@FieldDrawerType(Vector3f.class)
public class Vector3fDrawer extends InlineClassDrawer {

	public Vector3fDrawer() {
		super("x", "y", "z");
	}
}