package com.codeshaper.jello.engine.asset;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.apache.commons.io.IOUtils;
import org.lwjgl.system.MemoryStack;

import com.codeshaper.jello.engine.AssetFileExtension;
import com.codeshaper.jello.engine.AssetLocation;
import com.codeshaper.jello.engine.Debug;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBImage.*;

@AssetFileExtension(".png")
@AssetFileExtension(".bmp")
@AssetFileExtension(".gif")
public class Texture extends Asset {

	private int textureId;

	public Texture(AssetLocation location) {
		super(location);
	}

	@Override
	public void load() {
		super.load();

		try (MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer w = stack.mallocInt(1);
			IntBuffer h = stack.mallocInt(1);
			IntBuffer channels = stack.mallocInt(1);

			try(InputStream stream = location.getInputSteam()) {
				byte[] byteArray = IOUtils.toByteArray(stream);
				ByteBuffer bytes = stack.bytes(byteArray);
				ByteBuffer buf = stbi_load_from_memory(bytes, w, h, channels, 4);

				if (buf == null) {
					Debug.logError("Error loading Texture: " + stbi_failure_reason());
				}

				int width = w.get();
				int height = h.get();

				this.generateTexture(width, height, buf);

				stbi_image_free(buf);	
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void unload() {
		glDeleteTextures(textureId);
	}
	
	public void bind() {
		glBindTexture(GL_TEXTURE_2D, textureId);
	}

	private void generateTexture(int width, int height, ByteBuffer buf) {
		textureId = glGenTextures();

		glBindTexture(GL_TEXTURE_2D, textureId);
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
		glGenerateMipmap(GL_TEXTURE_2D);
	}
}
