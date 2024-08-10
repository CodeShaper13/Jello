package com.codeshaper.jello.engine.lighting;

import com.codeshaper.jello.editor.property.modifier.MinValue;
import com.codeshaper.jello.engine.Color;
import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.JelloComponent;

public abstract class AbstractLight extends JelloComponent {

	protected static final Color gizmoColor = new Color(1f, 1f, 0.75f);
	
	public Color color = new Color(1f, 1f, 0.75f);
	@MinValue(0f)
	public float intensity = 1f;
		
	public AbstractLight(GameObject owner) {
		super(owner);
	}
}
