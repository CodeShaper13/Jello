package com.codeshaper.jello.editor.menu;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.codeshaper.jello.editor.JelloEditor;

import ModernDocking.Dockable;
import ModernDocking.app.DockableMenuItem;
import ModernDocking.app.Docking;
import ModernDocking.app.LayoutsMenu;

public class WindowMenu extends JMenu {

	public WindowMenu() {
		super("Window");

		this.add(new LayoutsMenu());

		JMenuItem open = new JMenu("Open");
		for (Dockable dockable : Docking.getDockables()) {
			open.add(new DockableMenuItem(dockable.getPersistentID(), dockable.getTabText()));
		}
		this.add(open);
		
		this.addSeparator();
		
		JMenu themes = new JMenu("Theme");			
		
		JMenuItem dark = new JMenuItem("Dark");
		dark.addActionListener((e) -> {
			JelloEditor.instance.window.setDarkMode(true);
		});
		JMenuItem light = new JMenuItem("Light");
		light.addActionListener((e) -> {
			JelloEditor.instance.window.setDarkMode(true);
		});
		
		themes.add(dark);
		themes.add(light);
		
		this.add(themes);
	}
}
