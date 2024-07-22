package com.codeshaper.jello.editor.inspector;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.codeshaper.jello.editor.GuiLayoutBuilder;
import com.codeshaper.jello.engine.asset.Asset;

public class AssetEditor<T extends Asset> extends Editor<T> {

	public AssetEditor(T target) {
		super(target);
	}
	
	@Override
	public void drawInInsepctor(JPanel panel) {
		super.drawInInsepctor(panel);
		
		panel.setLayout(new BorderLayout());
		
		// Header.
		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));
		headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
		this.drawHeader(headerPanel);
		panel.add(headerPanel, BorderLayout.NORTH);		
		
		GuiLayoutBuilder drawer = new GuiLayoutBuilder();
		this.drawAsset(drawer);
		
		JPanel gridBagPanel = new JPanel(new GridBagLayout());
		GridBagConstraints constraint = new GridBagConstraints();
		constraint.anchor = GridBagConstraints.FIRST_LINE_START;
		constraint.fill = GridBagConstraints.HORIZONTAL;
		constraint.weightx = 1.0f;
		constraint.weighty = 1.0f;
		gridBagPanel.add(drawer.getPanel(), constraint);		
		
		JScrollPane scrollPane = new JScrollPane(
				gridBagPanel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(""), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		
		panel.add(scrollPane, BorderLayout.CENTER);
	}
	
	protected void drawAsset(GuiLayoutBuilder drawer) {
		drawer.addAll(this.target);
	}
	
	protected void drawHeader(JPanel headerPanel) {
		JLabel label = new JLabel(this.target.getAssetName());
		headerPanel.add(label);
	}
}
