package com.codeshaper.jello.engine.rendering;

import com.codeshaper.jello.engine.Color;
import com.codeshaper.jello.engine.ComponentName;
import com.codeshaper.jello.engine.JelloComponent;
import com.codeshaper.jello.engine.asset.Texture;

@ComponentName("Sprite Renderer")
public final class SpriteRenderer extends JelloComponent {

	public Texture sprite;
	public Color color = Color.white;
	public boolean flipX;
	public boolean flipY;
	public int sortOrder;
}
