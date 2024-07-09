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
	public Editor<?> getInspectorDrawer(JPanel panel) {
		return new TextAssetEditor(this, panel);
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
			this.lines = Files.readAllLines(this.file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class TextAssetEditor extends AssetEditor {

		public TextAssetEditor(Asset target, JPanel panel) {
			super(target, panel);
		}

		@Override
		public void drawAsset(JPanel panel) {
			panel.setLayout(new BorderLayout());
			JTextArea textArea = new JTextArea();
			String text = "";
			for (int i = 0; i < getLineCount(); i++) {
				text += getLine(i);
				if (i != (getLineCount() - 1)) {
					text += "\n";
				}
			}
			textArea.setText(text);
			textArea.setEnabled(false);

			panel.add(textArea, BorderLayout.CENTER);
		}
	}
}
