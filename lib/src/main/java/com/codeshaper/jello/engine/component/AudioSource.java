package com.codeshaper.jello.engine.component;

import com.codeshaper.jello.editor.property.modifier.Range;
import com.codeshaper.jello.engine.ComponentIcon;
import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.asset.AudioClip;

@ComponentIcon("/editor/componentIcons/audioSource.png")
public class AudioSource extends JelloComponent {

	public AudioClip audioClip;
	@Range(min = 0f, max = 1f)
	public float volume = 1f;
	public boolean playOnStart = false;
	public boolean loop = false;
	
	public AudioSource(GameObject owner) {
		super(owner);
	}
	
	public void play() {
		// TODO
	}
	
	public void pause() {
		// TODO
	}
	
	public void stop() {
		// TODO
	}
	
	public boolean isPlaying() {
		return false; // TODO
	}
}
