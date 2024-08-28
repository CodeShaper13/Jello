package com.codeshaper.jello.engine.audio;

import org.joml.Vector3f;

import com.codeshaper.jello.editor.GuiLayoutBuilder;
import com.codeshaper.jello.editor.inspector.ComponentEditor;
import com.codeshaper.jello.editor.inspector.Editor;
import com.codeshaper.jello.editor.property.modifier.Range;
import com.codeshaper.jello.engine.Application;
import com.codeshaper.jello.engine.ComponentIcon;
import com.codeshaper.jello.engine.ComponentName;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.JelloComponent;

import static org.lwjgl.openal.AL10.*;

import javax.swing.JPanel;

@ComponentName("Audio/Audio Listener")
@ComponentIcon("/editor/componentIcons/audioListener.png")
public final class AudioListener extends JelloComponent {

	/**
	 * The number of Audio Listeners in the Scene.
	 */
	private static int audioListenerCount = 0;
	
	@Range(min = 0f, max = 1f)
	public float volume = 1f;
	
	private transient Vector3f worldPosLastFrame;
	private transient float[] data;
	
	@Override
	protected void onStart() {
		super.onStart();

		audioListenerCount++;
		
		Vector3f pos = this.gameObject().getPosition(this.worldPosLastFrame);
		alListener3f(AL_POSITION, pos.x, pos.y, pos.z);
		alListener3f(AL_VELOCITY, 0, 0, 0);
		
		this.data = new float[6];
	}
	
	@Override
	protected void onUpdate(float deltaTime) {
		super.onUpdate(deltaTime);
		
		if(audioListenerCount > 1) {
			Debug.logWarning("There are %s Audio Listeners active, there should only be at most 1", audioListenerCount);
		}
		
		GameObject owner = this.gameObject();
		
		Vector3f pos = owner.getPosition();
		alListener3f(AL_POSITION, pos.x, pos.y, pos.z);
		
		Vector3f up = owner.getUp();
		Vector3f forward = owner.getForward();
        this.data[0] = forward.x;
        this.data[1] = forward.y;
        this.data[2] = forward.z;
        this.data[3] = up.x;
        this.data[4] = up.y;
        this.data[5] = up.z;        
        alListenerfv(AL_ORIENTATION, data);
		
		Vector3f velocity = pos.sub(this.worldPosLastFrame).mul(deltaTime);
		alListener3f(AL_VELOCITY, velocity.x, velocity.y, velocity.z);
		
		owner.getPosition(this.worldPosLastFrame);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		audioListenerCount--;
	}
	
	@Override
	public Editor<?> getInspectorDrawer(JPanel panel) {
		return new AudioListenerDrawer(this, panel);
	}
	
	public void setVolume(float v) {
		this.volume = v;
		
		if(Application.isPlaying()) {
			alListenerf(AL_GAIN, v);
		}
	}

	public float getVolume() {
	    return alGetListenerf(AL_GAIN);
	}
	
	private class AudioListenerDrawer extends ComponentEditor<AudioListener> {

		public AudioListenerDrawer(AudioListener component, JPanel panel) {
			super(component, panel);
		}

		@Override
		public void drawComponent(GuiLayoutBuilder builder) {
			builder.floatField("Volume", volume, (v) -> setVolume(v));
		}
	}
}
