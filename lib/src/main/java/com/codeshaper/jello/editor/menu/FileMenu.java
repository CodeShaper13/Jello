package com.codeshaper.jello.editor.menu;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Path;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.event.PlayModeListener.State;
import com.codeshaper.jello.engine.Debug;

public class FileMenu extends JMenu {

	public FileMenu() {
		super("File");

		JMenuItem save = new JMenuItem("Save");
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		save.addActionListener((e) -> {
			JelloEditor.instance.saveProject();
		});
		this.add(save);

		JMenuItem openInExplorer = new JMenuItem("Open Project In Explorer");
		openInExplorer.addActionListener((e) -> {
			Path assetsFolder = JelloEditor.instance.rootProjectFolder;
			try {
				Desktop.getDesktop().open(assetsFolder.toFile());
			} catch (IOException e1) {
				Debug.logWarning("Couldn't open %s", assetsFolder);
			}
		});
		this.add(openInExplorer);

		JMenuItem exit = new JMenuItem("Exit");
		exit.addActionListener((e) -> {
			System.exit(0);
		});
		this.add(exit);
		
		JelloEditor.instance.addPlayModeListener((state) -> {
			if(state == State.STARTED) {
				save.setEnabled(false);
			} else if(state == State.STOPPED) {
				save.setEnabled(true);
			}
		});
	}
}
