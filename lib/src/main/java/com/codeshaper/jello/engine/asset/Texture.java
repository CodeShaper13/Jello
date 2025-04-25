package com.codeshaper.jello.engine.asset;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.apache.commons.lang3.NotImplementedException;
import org.joml.Math;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

import com.codeshaper.jello.editor.GuiLayoutBuilder;
import com.codeshaper.jello.editor.inspector.AssetEditor;
import com.codeshaper.jello.editor.inspector.Editor;
import com.codeshaper.jello.engine.AssetFileExtension;
import com.codeshaper.jello.engine.AssetLocation;
import com.codeshaper.jello.engine.Color;
import com.codeshaper.jello.engine.Debug;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBImage.*;

@AssetFileExtension(".png")
@AssetFileExtension(".bmp")
@AssetFileExtension(".gif")
@AssetFileExtension(".jpg")
public class Texture extends Asset {

	private int textureId;
	private int width;
	private int height;
	public ByteBuffer pixelBuffer;

	public Texture(AssetLocation location) {
		super(location);
	}

	/**
	 * Creates a runtime {@link Texture}.
	 * 
	 * @param width
	 * @param height
	 * @param buffer
	 */
	public Texture(int width, int height, ByteBuffer pixels) {
		super(null);

		if (width <= 0) {
			throw new IllegalArgumentException("width must be greater than 0");
		}

		if (height <= 0) {
			throw new IllegalArgumentException("height must be greater than 0");
		}
		
		this.width = width;
		this.height = height;
		this.pixelBuffer = pixels;

		this.generateTexture();
	}

	@Override
	public void load() {
		super.load();

		try (MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer w = stack.mallocInt(1);
			IntBuffer h = stack.mallocInt(1);
			IntBuffer channels = stack.mallocInt(1);

			try (InputStream stream = location.getInputSteam()) {
				//byte[] byteArray = stream.readAllBytes();
				//ByteBuffer bytes = stack.bytes(byteArray);
				//ByteBuffer buf = stbi_load_from_memory(bytes, w, h, channels, 0);

				this.pixelBuffer = stbi_load_from_memory(this.ioResourceToByteBuffer(stream, 1024), w, h, channels, 4);

				if (this.pixelBuffer == null) {
					Debug.logError("Error loading Texture: " + stbi_failure_reason());
				}

				this.width = w.get();
				this.height = h.get();

				this.generateTexture();

				//stbi_image_free(this.pixelBuffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Deletes the texture from the GPU.
	 */
	@Override
	public void unload() {
		glDeleteTextures(this.textureId);
	}

	@Override
	public Editor<?> getInspectorDrawer(JPanel panel) {
		return new TextureEditor(this, panel);
	}

	/**
	 * Gets the width of the {@link Texture} in pixels.
	 * 
	 * @return the Texture's width
	 */
	public int getWidth() {
		return this.width;
	}

	/**
	 * Gets the height of the {@link Texture} in pixels.
	 * 
	 * @return the Texture's height
	 */
	public int getHeight() {
		return this.height;
	}

	/**
	 * Gets a pixel from the {@link Texture} at ({@code x}, {@code y}). ({@code 0},
	 * {@code 0}) is the bottom left, and ({@link Texture#getWidth()},
	 * {@link Texture#getHeight()} is the top right.
	 * <p>
	 * A new {@link Color} is allocated with every call. To avoid allocating a new
	 * object, use {@link Texture#getPixel(int, int, Color)} instead.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public Color getPixel(int x, int y) {
		int index = (x + (y * this.height)) * 4;
		return new Color(
				this.pixelBuffer.get(index),
				this.pixelBuffer.get(index + 1),
				this.pixelBuffer.get(index + 2),
				this.pixelBuffer.get(index + 3));
	}

	/**
	 * Get's a pixel from the {@link Texture} at ({@code x}, {@code y}). ({@code 0},
	 * {@code 0}) is the bottom left, and ({@link Texture#getWidth()},
	 * {@link Texture#getHeight()} is the top right.
	 * 
	 * @param x
	 * @param y
	 * @param color the Color to assign the pixel's color to
	 * @return
	 */
	public Color getPixel(int x, int y, Color color) {
		throw new NotImplementedException();
	}

	/**
	 * Sets a pixel on the {@link Texture}.
	 * <p>
	 * After changing pixels, {@link Texture#apply()} must be called to upload the
	 * changes to the GPU.
	 * 
	 * @param x
	 * @param y
	 * @param color
	 * @see Texture#apply()
	 */
	public void setPixel(int x, int y, Color color) {
		int index = (x + (y * this.height)) * 4;
		this.pixelBuffer.put(index, (byte) (Math.round(color.r * 255f)));
		this.pixelBuffer.put(index + 1, (byte) (Math.round(color.g * 255f)));
		this.pixelBuffer.put(index + 2, (byte) (Math.round(color.b * 255f)));
		this.pixelBuffer.put(index + 3, (byte) (Math.round(color.a * 255f)));
	}

	/**
	 * Uploads changes made to the {@link Texture} to the GPU. This will always
	 * upload the entire texture, without checking if any changes have actually been
	 * made. Because the entire texture is uploaded, the number of changed pixel has
	 * no effect on performance.
	 */
	public void apply() {
		glBindTexture(GL_TEXTURE_2D, this.textureId);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, this.width, this.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, this.pixelBuffer);
		glGenerateMipmap(GL_TEXTURE_2D);
	}

	void bind() {
		glBindTexture(GL_TEXTURE_2D, this.textureId);
	}

	private void generateTexture() {
		this.textureId = glGenTextures();

		glBindTexture(GL_TEXTURE_2D, this.textureId);
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		this.apply();
	}

	private ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
		ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
		buffer.flip();
		newBuffer.put(buffer);
		return newBuffer;
	}

	private ByteBuffer ioResourceToByteBuffer(InputStream source, int bufferSize) throws IOException {
		ByteBuffer buffer = BufferUtils.createByteBuffer(bufferSize);
		try {
			byte[] buf = new byte[8192];
			while (true) {
				int bytes = source.read(buf, 0, buf.length);
				if (bytes == -1)
					break;
				if (buffer.remaining() < bytes)
					buffer = this.resizeBuffer(buffer,
							Math.max(buffer.capacity() * 2, buffer.capacity() - buffer.remaining() + bytes));
				buffer.put(buf, 0, bytes);
			}
			buffer.flip();
		} finally {
			source.close();
		}
		return buffer;
	}

	private class TextureEditor extends AssetEditor<Texture> {

		public TextureEditor(Texture target, JPanel panel) {
			super(target, panel);
		}

		@Override
		protected void drawAsset(GuiLayoutBuilder builder) {
			File file = this.target.location.getFile();

			ImageIcon image = new ImageIcon(file.toString());
			
			builder.image(image, 100, 100);
			builder.label(String.format("Dimensions: %sx%s",
					image.getIconWidth(),
					image.getIconHeight()));
		}
	}
}
