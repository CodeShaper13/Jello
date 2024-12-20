package com.codeshaper.jello.editor.menu;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.codeshaper.jello.editor.JelloEditor;

public class HelpMenu extends JMenu {

	public HelpMenu() {
		super("Help");

		JMenuItem documentation = new JMenuItem("Documentaion");
		documentation.addActionListener((e) -> {
			openUrl(JelloEditor.DOCUMENTAION_URL);
		});
		this.add(documentation);

		JMenuItem reportIssue = new JMenuItem("Report Issue");
		reportIssue.addActionListener((e) -> {
			openUrl(JelloEditor.REPORT_ISSUE_URL);
		});
		this.add(reportIssue);

		JMenuItem about = new JMenuItem("About");
		about.addActionListener((e) -> {
			String message = String.format("Version: %s", JelloEditor.EDITOR_VERSION);
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