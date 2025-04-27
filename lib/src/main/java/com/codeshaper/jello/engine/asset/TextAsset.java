package com.codeshaper.jello.engine.asset;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.codeshaper.jello.editor.gui.GuiLayoutBuilder;
import com.codeshaper.jello.editor.inspector.AssetEditor;
import com.codeshaper.jello.editor.inspector.Editor;
import com.codeshaper.jello.engine.AssetFileExtension;
import com.codeshaper.jello.engine.AssetLocation;
import com.codeshaper.jello.engine.Debug;

@AssetFileExtension(".txt")
public class TextAsset extends Asset {

	private List<String> lines;

	public TextAsset(AssetLocation location) {
		super(location);

		this.lines = new ArrayList<String>();
	}

	@Override
	public void load() {
		super.load();
		
		try(InputStream stream = this.location.getInputSteam()) {
			this.lines = IOUtils.readLines(stream, StandardCharsets.UTF_8);
		} catch (IOException e) {
			Debug.log(e);
		}
	}

	@Override
	public void unload() {
		super.unload();

		this.lines = null; // Free memory.
	}

	@Override
	public Editor<?> getEditor(JPanel panel) {
		return new TextAssetEditor(this, panel);
	}

	/**
	 * Gets the number of lines in the file.
	 * 
	 * @return the number of lines in the file.
	 */
	public int getLineCount() {
		if (this.lines == null) {
			return 0;
		} else {
			return this.lines.size();
		}
	}

	/**
	 * Gets a specific line in the file. If the index is negative or greater than
	 * the line count, a {@link IndexOutOfBoundsException} is thrown.
	 * 
	 * @param lineNumber
	 * @return the line
	 */
	public String getLine(int lineNumber) {
		if (this.lines == null) {
			return StringUtils.EMPTY;
		} else {
			return this.lines.get(lineNumber);
		}
	}

	private class TextAssetEditor extends AssetEditor<TextAsset> {

		public TextAssetEditor(TextAsset target, JPanel panel) {
			super(target, panel);
		}

		@Override
		public void drawAsset(GuiLayoutBuilder drawer) {
			String text = "";
			for (int i = 0; i < this.target.getLineCount(); i++) {
				text += this.target.getLine(i);
				if (i != (this.target.getLineCount() - 1)) {
					text += "\n";
				}
			}

			drawer.textBox(null, text, 10);
		}
	}
}
