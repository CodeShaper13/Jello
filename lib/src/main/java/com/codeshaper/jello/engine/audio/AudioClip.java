package com.codeshaper.jello.engine.audio;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.apache.commons.io.IOUtils;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import com.codeshaper.jello.engine.AssetFileExtension;
import com.codeshaper.jello.engine.AssetLocation;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.asset.Asset;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.system.MemoryUtil.NULL;

@AssetFileExtension(".ogg")
public final class AudioClip extends Asset {

	private transient final int bufferId;
	private transient ShortBuffer pcm;
	
	public AudioClip(AssetLocation location) {
		super(location);

		this.bufferId = alGenBuffers();
		try (STBVorbisInfo info = STBVorbisInfo.malloc()) {
			this.pcm = this.readVorbis(location, info);

			// Copy to buffer
			alBufferData(this.bufferId, info.channels() == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16, pcm,
					info.sample_rate());
		}
	}

	@Override
	public void unload() {
		super.unload();

		alDeleteBuffers(this.bufferId);
		if (this.pcm != null) {
			MemoryUtil.memFree(pcm);
		}
	}

	/**
	 * Gets the id of the OpenAL buffer holding the audio data.
	 * 
	 * @return the id of the buffer holding the audio data.
	 */
	public int getBufferId() {
		return this.bufferId;
	}

	private ShortBuffer readVorbis(AssetLocation location, STBVorbisInfo info) {
		try (MemoryStack stack = MemoryStack.stackPush(); InputStream stream = location.getInputSteam()) {
			IntBuffer error = stack.mallocInt(1);
			ByteBuffer bytes = stack.bytes(IOUtils.toByteArray(stream));
			long decoder = stb_vorbis_open_memory(bytes, error, null);

			if (decoder == NULL) {
				Debug.logError("Failed to open Ogg Vorbis file. Error: " + error.get(0));
			}

			stb_vorbis_get_info(decoder, info);

			int channels = info.channels();
			int lengthSamples = stb_vorbis_stream_length_in_samples(decoder);

			ShortBuffer result = MemoryUtil.memAllocShort(lengthSamples * channels);

			result.limit(stb_vorbis_get_samples_short_interleaved(decoder, channels, result) * channels);
			stb_vorbis_close(decoder);

			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
