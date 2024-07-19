package com.codeshaper.jello.engine.asset;

import static org.lwjgl.opengl.GL46.*;

import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;

import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.inspector.AssetEditor;
import com.codeshaper.jello.editor.inspector.Editor;
import com.codeshaper.jello.editor.inspector.GuiDrawer;
import com.codeshaper.jello.engine.AssetFileExtension;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.Utils;

@AssetFileExtension(".frag")
@AssetFileExtension(".vert")
public class Shader extends Asset {

	private final ShaderType shaderType;
	private final int shaderId;
	private final boolean hasCompileError;

	public Shader(Path file) {
		super(file);

		System.out.println("instantiating shader " + file.toString());

		switch (FilenameUtils.getExtension(this.file.toString())) {
		case "frag":
			this.shaderType = ShaderType.FRAGMENT;
			break;
		case "vert":
			this.shaderType = ShaderType.VERTEX;
			break;
		default:
			this.shaderType = ShaderType.UNKNOW;
		}

		JelloEditor.instance.enableEditorContext();

		String code = Utils.readFile(file);
		this.shaderId = glCreateShader(this.shaderType.type);
		if (this.shaderId == 0) {
			Debug.logError("Unable to create shader");
		}
		glShaderSource(this.shaderId, code);
		glCompileShader(this.shaderId);
		
		this.hasCompileError = glGetShaderi(this.shaderId, GL_COMPILE_STATUS) == 0;
		if (this.hasCompileError) {
			System.out.println("Error compiling Shader code: " + glGetShaderInfoLog(this.shaderId, 1024));
		}
		
		JelloEditor.instance.disableEditorContext();
	}

	@Override
	public void cleanup() {
		super.cleanup();

		glDeleteShader(this.shaderId);
	}

	@Override
	public Editor<?> getInspectorDrawer() {
		return new ShaderEditor(this);
	}

	/**
	 * Gets the id of the Shader.
	 * 
	 * @return the Shader's id.
	 */
	public int getId() {
		return this.shaderId;
	}

	/**
	 * Gets the type of the Shader.
	 * 
	 * @return the Shader's type.
	 */
	public ShaderType getType() {
		return this.shaderType;
	}

	/**
	 * Checks if the Shader has a compile error.
	 * 
	 * @return {@code true} if there was a compile error, {@code false} if there was
	 *         not.
	 */
	public boolean hasCompileError() {
		return this.hasCompileError;
	}

	public String getCompileError() {
		JelloEditor.instance.enableEditorContext();
		String error = glGetShaderInfoLog(this.shaderId, 1024);
		JelloEditor.instance.disableEditorContext();
		return error;
	}

	public enum ShaderType {
		VERTEX(GL_VERTEX_SHADER), FRAGMENT(GL_FRAGMENT_SHADER), GEOMETRY(GL_GEOMETRY_SHADER),
		TESS_CONTROL(GL_TESS_CONTROL_SHADER), TESS_EVALUATION_SHADER(GL_TESS_EVALUATION_SHADER),
		COMPUTE_SHADER(GL_COMPUTE_SHADER), UNKNOW(-1);

		public final int type;

		ShaderType(int type) {
			this.type = type;
		}
	}

	private class ShaderEditor extends AssetEditor<Shader> {

		public ShaderEditor(Shader target) {
			super(target);
		}

		@Override
		protected void drawAsset(GuiDrawer drawer) {
			drawer.drawLabel("Shader Id: " + this.target.shaderId);
			drawer.drawLabel("Shader Type: " + this.target.shaderType);
			drawer.drawSpace(8);
			drawer.drawLabel("Compile Status:");
			String text;
			if (this.target.hasCompileError) {
				text = this.target.getCompileError();
			} else {
				text = "Success";
			}
			drawer.drawTextbox(text, 10);
		}
	}
}
