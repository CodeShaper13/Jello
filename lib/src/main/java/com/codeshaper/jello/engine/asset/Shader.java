package com.codeshaper.jello.engine.asset;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.codeshaper.jello.editor.GuiLayoutBuilder;
import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.inspector.AssetEditor;
import com.codeshaper.jello.editor.inspector.Editor;
import com.codeshaper.jello.engine.AssetFileExtension;
import com.codeshaper.jello.engine.AssetLocation;
import com.codeshaper.jello.engine.rendering.GameRenderer;
import com.codeshaper.jello.engine.rendering.ShaderData;
import com.codeshaper.jello.engine.rendering.ShaderProgram;
import com.codeshaper.jello.engine.rendering.UniformsMap;
import com.codeshaper.jello.engine.rendering.ShaderData.ShaderSource;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@AssetFileExtension(".shader")
public class Shader extends Asset {

	private ShaderData data;
	private ShaderProgram program;
	private UniformsMap uniformsMap;

	public Shader(AssetLocation location) {
		super(location);

		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();

		try (InputStream stream = location.getInputSteam(); Reader reader = new InputStreamReader(stream)) {
			this.data = gson.fromJson(reader, ShaderData.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		JelloEditor.instance.enableEditorContext();

		this.program = new ShaderProgram(this.data.shaders);
		this.uniformsMap = new UniformsMap(this.getProgramId());
		
		// Create the always present uniforms.
		this.uniformsMap.createUniform(GameRenderer.PROJECTION_MATRIX);
        this.uniformsMap.createUniform(GameRenderer.VIEW_MATRIX);
        this.uniformsMap.createUniform(GameRenderer.GAME_OBJECT_MATRIX);
        this.uniformsMap.createUniform(GameRenderer.TXT_SAMPLER);
		
		JelloEditor.instance.disableEditorContext();
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
	
	public int getProgramId() {
		return this.program.getProgramId();
	}
	
	public ShaderProgram getProgram() {
		return this.program;
	}
	
	public UniformsMap getUniformMap() {
		return this.uniformsMap;
	}
	
	private class ShaderEditor extends AssetEditor<Shader> {

		public ShaderEditor(Shader target) {
			super(target);
		}

		@Override
		protected void drawAsset(GuiLayoutBuilder drawer) {
			drawer.label("Program Id: " + this.target.getProgramId());
			drawer.space(8);
			drawer.label("Shaders:");
			for(ShaderSource module : this.target.data.shaders) {
				drawer.label("Type: " + module.getType());
				drawer.textBox(null, module.getSource(), 10);
			}
		}
	}
}
