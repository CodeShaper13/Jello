package com.codeshaper.jello.engine.asset;

import java.awt.BorderLayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.codeshaper.jello.editor.inspector.AssetEditor;
import com.codeshaper.jello.editor.inspector.Editor;
import com.codeshaper.jello.editor.inspector.GuiDrawer;
import com.codeshaper.jello.engine.AssetFileExtension;

@AssetFileExtension(".txt")
public class TextAsset extends Asset {

	private List<String> lines;

	public TextAsset(Path file) {
		super(file);

		this.lines = new ArrayList<String>();

		this.read();
	}

	@Override
	public void cleanup() {
		super.cleanup();

		this.lines = null; // Free memory.
	}

	@Override
	public Editor<?> getInspectorDrawer() {
		return new TextAssetEditor(this);
	}

	/**
	 * Gets the number of lines in the file.
	 * 
	 * @return the number of lines in the file.
	 */
	public int getLineCount() {
		return this.lines.size();
	}

	/**
	 * Gets a specific line in the file. If the index is negative or greater than
	 * the line count, a {@link IndexOutOfBoundsException} is thrown.
	 * 
	 * @param lineNumber
	 * @return the line
	 */
	public String getLine(int lineNumber) {
		return this.lines.get(lineNumber);
	}

	private void read() {
		try {
			Path fullPath = this.getFullPath();
			this.lines = Files.readAllLines(fullPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class TextAssetEditor extends AssetEditor<TextAsset> {

		public TextAssetEditor(TextAsset target) {
			super(target);
		}

		@Override
		public void drawAsset(GuiDrawer drawer) {
			//panel.setLayout(new BorderLayout());
			//JTextArea textArea = new JTextArea();
			String text = "";
			for (int i = 0; i < this.target.getLineCount(); i++) {
				text += this.target.getLine(i);
				if (i != (this.target.getLineCount() - 1)) {
					text += "\n";
				}
			}
			//textArea.setText(text);
			//textArea.setEnabled(false);
			
			drawer.drawTextbox(text, 10);


			//panel.add(textArea, BorderLayout.CENTER);
		}
	}
}
