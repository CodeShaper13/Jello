package com.codeshaper.jello.engine.asset;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;

import com.codeshaper.jello.editor.GuiLayoutBuilder;
import com.codeshaper.jello.editor.inspector.AssetEditor;
import com.codeshaper.jello.editor.inspector.Editor;
import com.codeshaper.jello.engine.AssetFileExtension;
import com.codeshaper.jello.engine.AssetLocation;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.rendering.GameRenderer;
import com.codeshaper.jello.engine.rendering.ShaderData;
import com.codeshaper.jello.engine.rendering.ShaderData.Property;
import com.codeshaper.jello.engine.rendering.ShaderProgram;
import com.codeshaper.jello.engine.rendering.ShaderSource;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

@AssetFileExtension(".shader")
public class Shader extends Asset {

	private ShaderData data;
	private ShaderProgram program;
	/**
	 * If there was an error creating the shader, it will be here. If there was no
	 * error, it will be null.
	 */
	private String error;

	public Shader(AssetLocation location) {
		super(location);

		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();

		StrBuilder errorBuilder = new StrBuilder();

		try (InputStream stream = location.getInputSteam(); Reader reader = new InputStreamReader(stream)) {
			this.data = gson.fromJson(reader, ShaderData.class);
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			errorBuilder.appendln("Error parsing JSON. " + e.getMessage());
		} catch (JsonIOException e) {
			errorBuilder.appendln("Error reading JSON. " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.program = new ShaderProgram(this.data.shaders);

		// Create the default uniforms.
		this.program.createUniform(GameRenderer.PROJECTION_MATRIX);
		this.program.createUniform(GameRenderer.VIEW_MATRIX);
		this.program.createUniform(GameRenderer.GAME_OBJECT_MATRIX);

		// Create the uniforms defined by the shader's properties.
		for (Property property : this.data.properties) {
			String uniform = property.uniform;
			if (this.program.doesUniformExist(uniform)) {
				this.program.createUniform(property.uniform);
			} else {
				Debug.logWarning(
						"Error in Shader \"%s\".  A property is defined, \"%s\" without a matching uniform.",
						location.getName(),
						uniform);
			}
		}

		if (errorBuilder.size() > 0) {
			this.error = errorBuilder.toString();
			Debug.logErrorWithContext(this, "Error creating shader \"" + location.getName() + "\"");
		}
	}

	@Override
	public void cleanup() {
		super.cleanup();

		this.program.cleanup();
	}

	@Override
	public Editor<?> getInspectorDrawer() {
		return new ShaderEditor(this);
	}

	/**
	 * Gets the Shader's program id. If the shader file is invalid or there was some
	 * other error, 0 is returned indicating an error.
	 * 
	 * @return the Shader's program id.
	 */
	public int getProgramId() {
		return this.program.programId;
	}

	public ShaderData getData() {
		return this.data;
	}

	public ShaderProgram getProgram() {
		return this.program;
	}

	public boolean hasError() {
		return this.error != null;
	}

	/**
	 * Returns the Shader's error message. If there was no error creating the
	 * shader, {@code null} is returned.
	 * 
	 * @return the Shader's error message.
	 */
	public String getError() {
		return this.error;
	}

	private class ShaderEditor extends AssetEditor<Shader> {

		public ShaderEditor(Shader target) {
			super(target);
		}

		@Override
		protected void drawAsset(GuiLayoutBuilder drawer) {
			if (this.target.hasError()) {
				drawer.label("There was a problem creating the Shader.");
				System.out.println(this.target.getError());
				drawer.textBox("Error:", this.target.getError(), 10);
			} else {
				drawer.label("Program Id: " + this.target.getProgramId());
				drawer.space(8);
				drawer.label("Shaders:");
				for (ShaderSource module : this.target.data.shaders) {
					drawer.label("Type: " + module.getType());
					drawer.textBox(null, module.getSource(), 10);
				}
			}
		}
	}
}
