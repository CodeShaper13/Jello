package com.codeshaper.jello.engine.audio;

import org.lwjgl.openal.*;

import com.codeshaper.jello.engine.Debug;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.openal.AL11.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public final class SoundManager {

	private static long context;
	private static long device;
	
	private SoundManager() { }

	public static boolean initialize() {
		if (isInitialized()) {
			return true;
		}

		device = alcOpenDevice((ByteBuffer) null);
		if (device == NULL) {
			Debug.logError("Failed to open the default OpenAL device.  Are there speakers or headphones plugged in?");
			return false;
		}

		context = alcCreateContext (device, (IntBuffer) null);
		if (context == NULL) {
			Debug.logError("Failed to create OpenAL context.");
			return false;
		}

		alcMakeContextCurrent(context);

		ALCCapabilities deviceCaps = ALC.createCapabilities(device);
		AL.createCapabilities(deviceCaps);

		alDistanceModel(AL_EXPONENT_DISTANCE);
		
		return true;
	}

	public static boolean isInitialized() {
		return device != NULL;
	}

	public static void shutdown() {
		alcMakeContextCurrent(0);
		
		if (context != NULL) {
			alcDestroyContext(context);
			context = NULL;
		}
		if (device != NULL) {
			alcCloseDevice(device);
			device = NULL;
		}
	}
}
