package com.codeshaper.jello.engine.rendering;

import org.joml.Vector3d;

import com.codeshaper.jello.editor.property.modifier.MinValue;
import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.JelloComponent;
import com.codeshaper.jello.engine.asset.Material;

public final class LineRenderer extends JelloComponent {

	public Vector3d[] points;
	public Material material;
	@MinValue(0f)
	public float width;
	public int pointVerticeCount;
	public int endcapVerticeCount;
	public boolean loop;

	public LineRenderer(GameObject owner) {
		super(owner);
	}
}