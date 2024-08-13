package com.codeshaper.jello.engine.audio;

import com.codeshaper.jello.editor.GuiLayoutBuilder;
import com.codeshaper.jello.editor.inspector.ComponentDrawer;
import com.codeshaper.jello.editor.property.modifier.Range;
import com.codeshaper.jello.editor.property.modifier.Space;
import com.codeshaper.jello.editor.property.modifier.ToolTip;
import com.codeshaper.jello.engine.Application;
import com.codeshaper.jello.engine.ComponentIcon;
import com.codeshaper.jello.engine.ComponentName;
import com.codeshaper.jello.engine.JelloComponent;

import static org.lwjgl.openal.AL10.*;

@ComponentName("Audio/Audio Source")
@ComponentIcon("/editor/componentIcons/audioSource.png")
public final class AudioSource extends JelloComponent {

	private AudioClip audioClip;
	private float pitch = 1f;
	@Range(min = 0f, max = 1f)
	private float gain = 1f;
	private boolean loop = false;
	private boolean is3d = true;

	@Space

	@ToolTip("If checked, the audio clip will play itself in onStart()")
	public boolean playOnStart = false;

	private transient int sourceId;

	@Override
	public void onConstruct() {
		this.sourceId = alGenSources();

		this.setPitch(this.pitch);
		this.setGain(this.gain);
		this.setLooping(this.loop);
		this.set3d(this.is3d);
	}

	@Override
	public void onStart() {
		super.onStart();

		this.setAudioClip(this.audioClip);

		if (this.playOnStart) {
			this.play();
		}
	}

	@Override
	public void onUpdate(float deltaTime) {
		super.onUpdate(deltaTime);

		// alSource3f(sourceId, AL_POSITION, position.x, position.y, position.z);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		this.stop();
		alDeleteSources(this.sourceId);
	}
	
	@Override
	public ComponentDrawer<?> getComponentDrawer() {
		return new AudioSourceDrawer(this);
	}

	/**
	 * Plays the set {@link AudioClip} from the beginning. If this Audio Source had
	 * been paused with {@link AudioSource#pause()}, this will resume the clip from
	 * where it was paused.
	 */
	public void play() {
		alSourcePlay(this.sourceId);
	}

	/**
	 * Pauses the sound. The clip can be resumed from the point it was paused at
	 * with {@link AudioSource#play()}.
	 */
	public void pause() {
		alSourcePause(this.sourceId);
	}

	/**
	 * Stops playing the clip.
	 */
	public void stop() {
		alSourceStop(this.sourceId);
	}

	/**
	 * Checks if the Audio Source is currently playing an {@link AudioClip}.
	 * 
	 * @return {@code true} if the AudioSource is playing a sound.
	 */
	public boolean isPlaying() {
		return alGetSourcei(this.sourceId, AL_SOURCE_STATE) == AL_PLAYING;
	}

	/**
	 * Gets the {@link AudioClip} that this Audio Source will play. If there is no
	 * Audio Clip set, {@code null} is returned.
	 * 
	 * @return the AudioClip that this Audio Source will play.
	 */
	public AudioClip getAudioClip() {
		return this.audioClip;
	}
	
	/**
	 * Sets the {@link AudioClip} this Audio Source is playing. This will stop the
	 * Audio Clip that is currently playing, even if the new clip is the same as the
	 * old one. Pass {@code null} to clear the clip.
	 * 
	 * @param clip the Audio Clip to use.
	 */
	public void setAudioClip(AudioClip clip) {
		boolean appIsPlaying = Application.isPlaying();
		
		if(appIsPlaying) {
			this.stop();
		}
		
		this.audioClip = clip;

		if(appIsPlaying) {
			alSourcei(this.sourceId, AL_BUFFER, clip == null ? 0 : clip.getBufferId());
		}
	}

	public float getPitch() {
		return this.pitch;
	}
	
	public void setPitch(float pitch) {
		this.pitch = pitch;
		
		if(Application.isPlaying()) {
			alSourcef(this.sourceId, AL_PITCH, pitch);
		}
	}

	public float getGain() {
		return this.gain;
	}
	
	public void setGain(float gain) {
		this.gain = gain;
		
		if(Application.isPlaying()) {
			alSourcef(this.sourceId, AL_GAIN, gain);
		}
	}

	public boolean isLooping() {
		return this.isLooping();
	}

	public void setLooping(boolean looping) {
		this.loop = looping;
		
		if(Application.isPlaying()) {
			alSourcei(this.sourceId, AL_LOOPING, looping ? AL_TRUE : AL_FALSE);
		}
	}

	public void set3d(boolean is3d) {
		this.is3d = is3d;
		
		if(Application.isPlaying()) {
			alSourcei(this.sourceId, AL_SOURCE_RELATIVE, is3d ? AL_TRUE : AL_FALSE);
		}
	}

	public boolean is3d() {
		return this.is3d;
	}

	private class AudioSourceDrawer extends ComponentDrawer<AudioSource> {

		public AudioSourceDrawer(AudioSource component) {
			super(component);
		}
		
		@Override
		protected void drawComponent(GuiLayoutBuilder builder) {			
			builder.assetField("Clip", getAudioClip(), AudioClip.class, (v) -> setAudioClip(v));
			builder.space();
			
			builder.floatField("Gain", gain, (v) -> setGain(v));
			builder.floatField("Pitch", AL_INVALID_VALUE, (v) -> setPitch(v));
			builder.checkbox("Loop", loop, (v) -> setLooping(loop));
			
			builder.space();
			builder.checkbox("Play in onStart", playOnStart, (v) -> playOnStart = v);
		}		
	}
}
