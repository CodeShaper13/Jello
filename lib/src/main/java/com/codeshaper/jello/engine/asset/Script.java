package com.codeshaper.jello.engine.asset;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

import com.codeshaper.jello.editor.GuiLayoutBuilder;
import com.codeshaper.jello.editor.inspector.AssetEditor;
import com.codeshaper.jello.editor.inspector.Editor;
import com.codeshaper.jello.engine.AssetFileExtension;
import com.codeshaper.jello.engine.AssetLocation;

@AssetFileExtension("java")
public final class Script extends Asset {

	private String source;

	public Script(AssetLocation location) {
		super(location);

		try (InputStream stream = location.getInputSteam()) {
			this.source = String.join(IOUtils.toString(stream, StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Editor<?> getInspectorDrawer() {
		return new ScriptEditor(this);
	}

	private class ScriptEditor extends AssetEditor<Script> {

		public ScriptEditor(Script target) {
			super(target);
		}

		@Override
		protected void drawAsset(GuiLayoutBuilder drawer) {
			super.drawAsset(drawer); // Draws default inspector

			drawer.label("Compilation Errors:");
			drawer.textBox(null, "No Errors", 10);
		}
	}
}
