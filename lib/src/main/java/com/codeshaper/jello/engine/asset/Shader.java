package com.codeshaper.jello.engine.asset;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.swing.JPanel;

import com.codeshaper.jello.editor.GuiLayoutBuilder;
import com.codeshaper.jello.editor.inspector.AssetEditor;
import com.codeshaper.jello.editor.inspector.Editor;
import com.codeshaper.jello.engine.AssetFileExtension;
import com.codeshaper.jello.engine.AssetLocation;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.rendering.shader.ShaderData;
import com.codeshaper.jello.engine.rendering.shader.ShaderProgram;
import com.codeshaper.jello.engine.rendering.shader.ShaderSource;
import com.google.gson.Gson;
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
	}
	
	@Override
	public void load() {
		super.load();
		
		Gson gson = new Gson();

		try (InputStream stream = location.getInputSteam(); Reader reader = new InputStreamReader(stream)) {
			this.data = gson.fromJson(reader, ShaderData.class);
		} catch (JsonSyntaxException e) {
			Debug.log(e, this);
			this.error = "Error parsing JSON. " + e.getMessage();
		} catch (JsonIOException e) {
			Debug.log(e, this);
			this.error = "Error reading JSON. " + e.getMessage();
		} catch (IOException e) {
			Debug.log(e, this);
		}

		if (this.data != null) {
			this.program = new ShaderProgram(this.data.shaders);
		}
	}

	@Override
	public void unload() {
		super.unload();

		this.data = null;
		if (this.program != null) {
			this.program.deleteProgram();
		}
	}

	@Override
	public Editor<?> getInspectorDrawer(JPanel panel) {
		return new ShaderEditor(this, panel);
	}

	/**
	 * Gets the Shader's Shader Program Id. If this Shader is invalid, 0 is
	 * returned.
	 * 
	 * @return the Shader's program id.
	 * @see Shader#isInvalid()
	 */
	public int getProgramId() {
		if (this.program != null) {
			return this.program.programId;
		} else {
			return 0;
		}
	}

	/**
	 * Gets the Shader's backing {@link ShaderData}. If this Shader is invalid, null
	 * is returned.
	 * 
	 * @return
	 * @see Shader#isInvalid()
	 */
	public ShaderData getData() {
		if(this.data != null) {
			return this.data;
		} else {
			return new ShaderData();
		}
	}

	public ShaderProgram getProgram() {
		return this.program;
	}

	/**
	 * Checks if the Shader is invalid or not. A Shader is invalid if there was an
	 * error during loading. this is often caused a JSON syntax error in the .shader
	 * file.
	 * <p>
	 * The error that caused the Shader to be invalid can be retrieved with
	 * {@link Shader#getError()}.
	 * 
	 * @return {@link true} is the Sahder is valid, {@link false} if there was some
	 *         error during loading.
	 */
	public boolean isInvalid() {
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

		public ShaderEditor(Shader target, JPanel panel) {
			super(target, panel);
		}

		@Override
		protected void drawAsset(GuiLayoutBuilder drawer) {
			if (this.target.isInvalid()) {
				drawer.label("There was a problem creating the Shader.");
				drawer.textBox("Error:", this.target.getError(), 10);
			} else {
				drawer.label("Program Id: " + this.target.getProgramId());
				drawer.space(8);
				drawer.label("Shaders:");
				for (ShaderSource module : this.target.data.shaders) {
					drawer.label("Type: " + module.type);
					drawer.textBox(null, module.source, 10);
				}
			}
		}
	}
}
