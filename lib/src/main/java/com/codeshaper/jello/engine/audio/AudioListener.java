package com.codeshaper.jello.engine.audio;

import org.joml.Math;
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

/**
 * {@link AudioListener} Components acts as a microphone and picks up sounds in
 * the Scene(s) to play in the devices' speakers . In most situation, this would
 * be attached to the Player or Main Camera. There should only be 1 active Audio
 * Listener Component at a time.
 */
@ComponentName("Audio/Audio Listener")
@ComponentIcon("/_editor/componentIcons/audioListener.png")
public final class AudioListener extends JelloComponent {

	/**
	 * The number of Audio Listeners in the Scene.
	 */
	private static int enabledAudioListenerCount = 0;

	@Range(min = 0f, max = 1f)
	public float volume = 1f;

	private transient Vector3f vec;
	private transient Vector3f worldPosLastFrame;
	private transient float[] data;

	@Override
	protected void onStart() {
		super.onStart();

		this.vec = new Vector3f();
		this.worldPosLastFrame = new Vector3f();
		this.data = new float[6];

		Vector3f pos = this.gameObject().getPosition(this.worldPosLastFrame);
		alListener3f(AL_POSITION, pos.x, pos.y, pos.z);
		alListener3f(AL_VELOCITY, 0, 0, 0);
	}

	@Override
	protected void onEnable() {
		super.onEnable();

		AudioListener.enabledAudioListenerCount++;
	}

	@Override
	protected void onDisable() {
		super.onDisable();

		AudioListener.enabledAudioListenerCount--;
	}

	@Override
	protected void onUpdate(float deltaTime) {
		super.onUpdate(deltaTime);

		if (enabledAudioListenerCount > 1) {
			Debug.logWarning("There are %s Audio Listeners active, there should only be at most 1",
					enabledAudioListenerCount);
		}

		GameObject gameObj = this.gameObject();

		// Update the rotation of the Audio Listener.
		gameObj.getForward(this.vec);
		this.data[0] = this.vec.x;
		this.data[1] = this.vec.y;
		this.data[2] = this.vec.z;
		gameObj.getUp(this.vec);
		this.data[3] = this.vec.x;
		this.data[4] = this.vec.y;
		this.data[5] = this.vec.z;
		alListenerfv(AL_ORIENTATION, this.data);

		// Update the position of the Audio Listener.
		gameObj.getPosition(this.vec);
		alListener3f(AL_POSITION, this.vec.x, this.vec.y, this.vec.z);

		// Update the velocity of the Audio Listener.
		Vector3f velocity = this.vec.sub(this.worldPosLastFrame).mul(deltaTime);
		alListener3f(AL_VELOCITY, velocity.x, velocity.y, velocity.z);

		gameObj.getPosition(this.worldPosLastFrame);
	}

	@Override
	public Editor<?> getEditor(JPanel panel) {
		return new AudioListenerDrawer(this, panel);
	}

	/**
	 * Sets the volume of the Audio Listener.
	 * 
	 * @param volume the volume of the listener, from 0 to 1.
	 */
	public void setVolume(float volume) {
		volume = Math.clamp(0f, 1f, volume);

		this.volume = volume;

		if (Application.isPlaying()) {
			alListenerf(AL_GAIN, volume);
		}
	}

	/**
	 * Gets the volume of the Audio Listener.
	 * 
	 * @return the volume, between 0 and 1.
	 */
	public float getVolume() {
		return alGetListenerf(AL_GAIN);
	}

	/**
	 * Gets the number of enabled {@link AudioListener} Components across all loaded
	 * Scenes.
	 * 
	 * @return the number of active {@link AudioListener} Components.
	 */
	public static int getEnabledAudioListenerCount() {
		return AudioListener.enabledAudioListenerCount;
	}

	/**
	 * A custom editor for AudioListener components. A custom editor is necessary
	 * because when the {@code volume} field is changed, OpenAL must be explicitly
	 * notified of the change.
	 */
	private class AudioListenerDrawer extends ComponentEditor<AudioListener> {

		public AudioListenerDrawer(AudioListener component, JPanel panel) {
			super(component, panel);
		}

		@Override
		public void drawComponent(GuiLayoutBuilder builder) {
			builder.floatSliderField("Volume", 0f, 1f, volume, (v) -> setVolume(v));
		}
	}
}
