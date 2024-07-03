package com.codeshaper.jello.editor.inspector;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import com.codeshaper.jello.engine.asset.Asset;

public class AssetEditor extends Editor<Asset> {

	public AssetEditor(Asset target, JPanel panel) {
		super(target, panel);
	}
	
	@Override
	public void draw() {
		super.draw();
		
		this.panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		
		// Header
		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
		this.drawHeader(headerPanel);
		gbc.gridy = 0;
		this.panel.add(headerPanel, gbc);
		
		JPanel p = new JPanel();
		this.drawAsset(p);
		gbc.gridy = 1;
		this.panel.add(p, gbc);
		
		// Filler panel.
		gbc.weighty = 1;
		gbc.gridy = 2;
		panel.add(new JPanel(), gbc);
	}
	
	protected void drawAsset(JPanel panel) {
		
	}
	
	protected void drawHeader(JPanel panel) {
		JLabel label = new JLabel(this.target.getAssetName());
		panel.add(label);
		panel.add(new JSeparator());
	}
}
