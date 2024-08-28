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

	protected Header header;
	
	private JPanel gridBagPanel;
	private GridBagConstraints constraints;
	
	public AssetEditor(T target, JPanel panel) {
		super(target, panel);
		
		this.panel.setLayout(new BorderLayout());
		
		this.panel.add(this.header = new Header(), BorderLayout.NORTH);
				
		this.gridBagPanel = new JPanel(new GridBagLayout());
		
		this.constraints = new GridBagConstraints();
		this.constraints.anchor = GridBagConstraints.FIRST_LINE_START;
		this.constraints.fill = GridBagConstraints.HORIZONTAL;
		this.constraints.weightx = 1.0f;
		this.constraints.weighty = 1.0f;
		
		JScrollPane scrollPane = new JScrollPane(
				this.gridBagPanel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(""), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		
		this.panel.add(scrollPane, BorderLayout.CENTER);
	}
	
	@Override
	public void onDraw(boolean isInitialDraw) {
		GuiLayoutBuilder builder = new GuiLayoutBuilder();
		this.drawAsset(builder);
				
		this.gridBagPanel.add(builder.getPanel(), this.constraints);		
		
		this.panel.revalidate();
	}
	
	@Override
	public void onRefresh() {
		super.onRefresh();
		
		this.gridBagPanel.removeAll();
	}
	
	protected void drawAsset(GuiLayoutBuilder builder) {
		builder.addAll(this.target);
	}
	
	private class Header extends JPanel {
		
		public JLabel label;
		
		public Header() {
			this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			this.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
			
			this.label = new JLabel(target.getAssetName());
			this.add(label);
		}
	}
}
