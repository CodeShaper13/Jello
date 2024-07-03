package com.codeshaper.jello.engine.component;

import com.codeshaper.jello.editor.property.modifier.Range;
import com.codeshaper.jello.engine.GameObject;

public class AudioListener extends JelloComponent {

	@Range(min = 0f, max = 1f)
	public float volume = 1f;
	
	public AudioListener(GameObject owner) {
		super(owner);
	}
}
