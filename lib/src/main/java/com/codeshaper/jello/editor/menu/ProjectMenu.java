package com.codeshaper.jello.editor.menu;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.codeshaper.jello.editor.JelloEditor;

public class ProjectMenu extends JMenu {

	public ProjectMenu() {
		super("Project");

		JMenuItem reload = new JMenuItem("Reload Project");
		reload.addActionListener(e -> {
			JelloEditor.instance.reloadProject();
		});
		this.add(reload);

		JCheckBoxMenuItem autoReload = new JCheckBoxMenuItem("Auto-Reload", JelloEditor.instance.properties.getBoolean("toolbar.auto_reload", true));
		autoReload.setToolTipText("If enabled, the project is reloaded whenever the application regains focus.");
		autoReload.addActionListener((e) -> {
			JelloEditor.instance.properties.setBoolean("toolbar.auto_reload", autoReload.isSelected());
		});
		this.add(autoReload);

		JelloEditor.instance.window.addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowGainedFocus(WindowEvent e) {
				if (autoReload.isSelected()) {
					JelloEditor.instance.reloadProject();
				}
			}
		});
	}
}