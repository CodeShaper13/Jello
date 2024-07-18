package com.codeshaper.jello.editor;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import com.codeshaper.jello.engine.Debug;

import ModernDocking.Dockable;
import ModernDocking.app.DockableMenuItem;
import ModernDocking.app.Docking;
import ModernDocking.app.LayoutsMenu;

public class EditorMenuBar extends JMenuBar {

	private final EditorMainFrame window;

	public EditorMenuBar(EditorMainFrame window, String reportIssueUrl, String documentationUrl) {
		this.window = window;

		this.add(new FileMenu());
		this.add(new EditMenu());
		this.add(new ProjectMenu());
		this.add(new PlayModeMenu());
		this.add(new WindowMenu());
		this.add(new HelpMenu(reportIssueUrl, documentationUrl));
	}

	private class FileMenu extends JMenu {

		public FileMenu() {
			super("File");

			JMenuItem save = new JMenuItem("Save Open Scene");
			save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
			save.addActionListener((e) -> {
				JelloEditor.instance.saveScene();
			});
			this.add(save);

			JMenuItem openInExplorer = new JMenuItem("Open In Explorer");
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
		}
	}

	private class EditMenu extends JMenu {

		public EditMenu() {
			super("Edit");

			JMenuItem undo = new JMenuItem("Undo");
			undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
			undo.addActionListener((e) -> {
				JelloEditor.instance.preformUndo();
			});
			this.add(undo);

			JMenuItem redo = new JMenuItem("Redo");
			redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
			redo.addActionListener((e) -> {
				JelloEditor.instance.preformRedo();
			});
			this.add(redo);

			this.addSeparator();

			JMenuItem cut = new JMenuItem("Cut");
			cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
			this.add(cut);

			JMenuItem copy = new JMenuItem("Copy");
			copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
			this.add(copy);

			JMenuItem paste = new JMenuItem("Paste");
			paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
			this.add(paste);
		}
	}

	private class PlayModeMenu extends JMenu {

		public PlayModeMenu() {
			super("Play Mode");

			JMenuItem run = new JMenuItem("Start");
			run.setToolTipText("Starts Play Mode with the Scene set to the \"Main Scene\"");
			run.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
			run.addActionListener((e) -> {
				JelloEditor.instance.startPlaymode();
			});
			this.add(run);

			JMenuItem runCurrent = new JMenuItem("Start Current");
			runCurrent.setToolTipText("Starts Play Mode with all of the currently open Scenes");
			runCurrent.setAccelerator(
					KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
			runCurrent.addActionListener((e) -> {
				JelloEditor.instance.startPlaymode();
			});
			this.add(runCurrent);
			
			this.addSeparator();
			
			JMenuItem stop = new JMenuItem("Stop");
			stop.setToolTipText("Stops Play Mode");
			stop.addActionListener((e) -> {
				JelloEditor.instance.stopPlaymode();
			});
			this.add(stop);
		}
	}

	private class WindowMenu extends JMenu {

		public WindowMenu() {
			super("Window");

			this.add(new LayoutsMenu());

			for (Dockable dockable : Docking.getDockables()) {
				this.add(new DockableMenuItem(dockable.getPersistentID(), "Open " + dockable.getTabText()));
			}
		}
	}

	private class HelpMenu extends JMenu {

		private final String reportIssueUrl;
		private final String documentationUrl;

		public HelpMenu(String reportIssueUrl, String documentationUrl) {
			super("Help");

			this.reportIssueUrl = reportIssueUrl;
			this.documentationUrl = documentationUrl;

			JMenuItem documentation = new JMenuItem("Documentaion");
			documentation.addActionListener((e) -> {
				openUrl(this.documentationUrl);
			});
			this.add(documentation);

			JMenuItem reportIssue = new JMenuItem("Report Issue");
			reportIssue.addActionListener((e) -> {
				openUrl(this.reportIssueUrl);
			});
			this.add(reportIssue);

			JMenuItem about = new JMenuItem("About");
			about.addActionListener((e) -> {
				String message = String.format("Version: {0}", JelloEditor.EDITOR_VERSION);
				JOptionPane.showMessageDialog(getParent(), message, "Version", JOptionPane.INFORMATION_MESSAGE);
			});
			this.add(about);
		}

		private void openUrl(String url) {
			if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				try {
					Desktop.getDesktop().browse(new URI(url));
				} catch (IOException | URISyntaxException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private class ProjectMenu extends JMenu {

		public ProjectMenu() {
			super("Project");

			JMenuItem reload = new JMenuItem("Reload Project");
			reload.addActionListener(e -> {
				JelloEditor.instance.reloadProject();
			});
			this.add(reload);

			JMenuItem autoReload = new JCheckBoxMenuItem("Auto-Reload");
			autoReload.setToolTipText("If enabled, the project is reloaded whenever the application regains focus.");
			this.add(autoReload);

			window.addWindowFocusListener(new WindowAdapter() {
				@Override
				public void windowGainedFocus(WindowEvent e) {
					if (autoReload.isSelected()) {
						JelloEditor.instance.reloadProject();
					}
				}
			});
		}
	}
}
