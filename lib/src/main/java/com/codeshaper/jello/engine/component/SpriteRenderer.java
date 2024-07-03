package com.codeshaper.jello.engine.component;

import com.codeshaper.jello.engine.Color;
import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.asset.Texture;

public class SpriteRenderer extends JelloComponent {

	public Texture sprite;
	public Color color = Color.white;
	public boolean flipX;
	public boolean flipY;
	public int sortOrder;

	public SpriteRenderer(GameObject owner) {
		super(owner);
	}
}
