package com.codeshaper.jello.engine.asset;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.codeshaper.jello.editor.inspector.AssetEditor;
import com.codeshaper.jello.editor.inspector.Editor;

public class TextAsset extends Asset {

	private final List<String> lines;
	
	public TextAsset(File file) {
		super(file);
		
		this.lines = new ArrayList<String>();

		this.read();
	}
	
	@Override
	public Editor<?> getInspectorDrawer(JPanel panel) {
		return new TextAssetEditor(this, panel);
	}
	
	public int getLineCount() {
		return this.lines.size();
	}
	
	public String getLine(int lineNumber) {
		return this.lines.get(lineNumber);
	}

	private void read() {
		try (BufferedReader br = new BufferedReader(new FileReader(this.file))) {
			String line = br.readLine();

			while (line != null) {
				this.lines.add(line);
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
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
			for(int i = 0; i < getLineCount(); i++) {
				text += getLine(i);
				if(i != (getLineCount() - 1)) {
					text += "\n";
				}
			}
			textArea.setText(text);
			textArea.setEnabled(false);
			
			panel.add(textArea, BorderLayout.CENTER);
		}		
	}
}
