package com.codeshaper.jello.engine.asset;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.io.IOUtils;
import org.lwjgl.system.MemoryStack;

import com.codeshaper.jello.editor.GuiLayoutBuilder;
import com.codeshaper.jello.editor.inspector.AssetEditor;
import com.codeshaper.jello.editor.inspector.Editor;
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

	public Texture(int width, int height, ByteBuffer buffer) {
		super(null);

		this.generateTexture(width, height, buffer);
	}

	@Override
	public void load() {
		super.load();

		try (MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer w = stack.mallocInt(1);
			IntBuffer h = stack.mallocInt(1);
			IntBuffer channels = stack.mallocInt(1);

			try (InputStream stream = location.getInputSteam()) {
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

	void bind() {
		glBindTexture(GL_TEXTURE_2D, this.textureId);
	}

	private void generateTexture(int width, int height, ByteBuffer buf) {
		this.textureId = glGenTextures();

		glBindTexture(GL_TEXTURE_2D, this.textureId);
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
		glGenerateMipmap(GL_TEXTURE_2D);
	}

	private class TextureEditor extends AssetEditor<Texture> {

		public TextureEditor(Texture target, JPanel panel) {
			super(target, panel);
		}

		@Override
		protected void drawAsset(GuiLayoutBuilder builder) {
			File file = this.target.location.getFile();
			ImageIcon image = new ImageIcon(file.toString());
			int textureWidth = image.getIconWidth();
			int textureHeight = image.getIconHeight();

			int sizeX = 100;
			int sizeY = 100;
			image = new ImageIcon(image.getImage().getScaledInstance(sizeX, sizeY, Image.SCALE_DEFAULT));
			
			JLabel label = new JLabel(image);
			label.setSize(sizeX, sizeY);
			builder.add(label);
			
			builder.label(String.format("Dimensions: %sx%s",
					textureWidth,
					textureHeight));
		}
	}
}
