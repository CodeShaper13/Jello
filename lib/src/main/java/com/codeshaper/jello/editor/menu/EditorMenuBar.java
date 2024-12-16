package com.codeshaper.jello.editor.menu;

import javax.swing.JMenuBar;

public class EditorMenuBar extends JMenuBar {

	public EditorMenuBar() {
		this.add(new FileMenu());
		this.add(new EditMenu());
		this.add(new ProjectMenu());
		this.add(new PlayModeMenu());
		this.add(new WindowMenu());
		this.add(new HelpMenu());
	}
}
