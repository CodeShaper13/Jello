package com.codeshaper.jello.editor.window;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.codeshaper.jello.editor.inspector.ComponentEditor;
import com.codeshaper.jello.editor.swing.TextPrompt;

public class FileSearchBar extends JPanel {	

	private static final ImageIcon SEARCH_ICON = new ImageIcon(
			ComponentEditor.class.getResource("/editor/icons/magnifying_glass.png"));

	private final JTextField searchField;
	
	public FileSearchBar(String ghostText) {
		this.setLayout(new GridBagLayout());
						
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 0);
		
		this.add(new JLabel(SEARCH_ICON), gbc);
		
		this.searchField = new JTextField();
		
		new TextPrompt(ghostText, this.searchField);
		
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.weightx = 1f;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		this.add(this.searchField, gbc);
	}
	
	public String getSearchText() {
		return this.searchField.getText();
	}
	
	public void setSearchText(String search) {
		this.searchField.setText(search);
	}
	
	public void addActionListener(ActionListener listener) {
		this.searchField.addActionListener(listener);
	}
	
	public void removeActionListener(ActionListener listener) {
		this.searchField.removeActionListener(listener);
	}
}
