package com.codeshaper.jello.editor.inspector;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.codeshaper.jello.engine.asset.Asset;

public class AssetEditor<T extends Asset> extends Editor<T> {

	public AssetEditor(T target) {
		super(target);
	}
	
	@Override
	public void draw(JPanel panel) {
		super.draw(panel);
		
        panel.setLayout(new BorderLayout());
		
		// Header.
		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));
		this.drawHeader(headerPanel);
		panel.add(headerPanel, BorderLayout.NORTH);		
		
		GuiDrawer drawer = new GuiDrawer();
		this.drawAsset(drawer);
		
		JScrollPane scrollPane = new JScrollPane(drawer.getPanel());
		panel.add(scrollPane, BorderLayout.CENTER);
	}
	
	protected void drawAsset(GuiDrawer drawer) {
		drawer.drawObject(this.target);
	}
	
	protected void drawHeader(JPanel headerPanel) {
		JLabel label = new JLabel(this.target.getAssetName());
		headerPanel.add(label);
	}
}
