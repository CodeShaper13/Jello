package com.codeshaper.jello.editor.menu;

import javax.swing.JMenuBar;

import com.codeshaper.jello.editor.EditorMainFrame;

public class EditorMenuBar extends JMenuBar {

	public EditorMenuBar(EditorMainFrame editorMainFrame) {
		this.add(new FileMenu());
		this.add(new EditMenu());
		this.add(new ProjectMenu(editorMainFrame));
		this.add(new PlayModeMenu());
		this.add(new WindowMenu());
		this.add(new HelpMenu());
	}
}
