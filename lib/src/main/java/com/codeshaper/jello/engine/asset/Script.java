package com.codeshaper.jello.engine.asset;

import java.nio.file.Path;

import com.codeshaper.jello.editor.GuiLayoutBuilder;
import com.codeshaper.jello.editor.inspector.AssetEditor;
import com.codeshaper.jello.editor.inspector.Editor;
import com.codeshaper.jello.engine.AssetFileExtension;

@AssetFileExtension("java")
public class Script extends Asset {

	public Script(Path path) {
		super(path);
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
			drawer.textbox("No Errors", 10);
		}
	}
}
