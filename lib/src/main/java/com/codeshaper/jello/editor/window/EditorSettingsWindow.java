package com.codeshaper.jello.editor.window;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;

import com.codeshaper.jello.editor.GuiLayoutBuilder;
import com.codeshaper.jello.editor.JelloEditor;

public class EditorSettingsWindow extends EditorWindow {
		
	public EditorSettingsWindow() {
		super("Editor Settings", "editorSettings");
		
		this.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		this.setLayout(new BorderLayout());
		
		JButton saveBtn = new JButton("Save");
		saveBtn.addActionListener((e) -> {
			System.out.println(JelloEditor.instance.settings.ideLocation);
			JelloEditor.instance.saveSettings();
		});
		this.add(saveBtn, BorderLayout.SOUTH);
		
		this.func();
	}
	
	private void func() {
		GuiLayoutBuilder builder = new GuiLayoutBuilder();
		
		builder.addAll(JelloEditor.instance.settings);
		this.add(builder.getPanel(), BorderLayout.NORTH);
	}
}