package com.codeshaper.jello.editor.inspector;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import com.codeshaper.jello.editor.gui.GuiBuilder;
import com.codeshaper.jello.editor.gui.GuiLayoutBuilder;
import com.codeshaper.jello.engine.GameObject;

public class GameObjectEditor extends Editor<GameObject> {

	private JButton addComponentButton;
	private JPanel componentListPanel;
	private JScrollPane componentScrollPane;

	public GameObjectEditor(GameObject target, JPanel panel) {
		super(target, panel);

		panel.setLayout(new BorderLayout());
		
		GuiLayoutBuilder builder = new GuiLayoutBuilder();
		
		// Header
		builder.startHorizontal();		
		builder.checkbox(null, this.target.isActive(), v -> {
			this.target.setActive(v);
		});		
		builder.textField(null, this.target.getName(), v -> {
			this.target.setName(v);
		});		
		builder.endHorizontal();
		
		// Transform
		GuiLayoutBuilder transformBuilder = builder.subPanel("Transform");
		transformBuilder.vector3fField("Position", this.target.getLocalPosition(), (v) -> {
			this.target.setLocalPosition(v);
		});
		transformBuilder.quaternionField("Rotation", this.target.getLocalRotation(), (v) -> {
			this.target.setLocalRotation(v);
		});
		transformBuilder.vector3fField("Scale", this.target.getLocalScale(), (v) -> {
			this.target.setLocalScale(v);
		});
		
		this.addComponentButton = GuiBuilder.button("Add Component", null, () -> {
			JDialog dialog = new AddComponentDialog((componentClass) -> {
				target.addComponent(componentClass);
				createComponentListPanel();

			});
			dialog.setVisible(true);
		});

		// Component List:
		this.componentListPanel = new JPanel();
		this.componentListPanel.setLayout(new BoxLayout(this.componentListPanel, BoxLayout.Y_AXIS));

		this.componentScrollPane = new JScrollPane(
				this.componentListPanel,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.componentScrollPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Components"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));		

		panel.add(builder.getPanel(), BorderLayout.NORTH);
		panel.add(this.componentScrollPane, BorderLayout.CENTER);
		panel.add(this.addComponentButton, BorderLayout.SOUTH);

		this.createComponentListPanel();
	}
	
	private void createComponentListPanel() {
		this.componentListPanel.removeAll();

		Border borderBox = BorderFactory.createTitledBorder("");
		Border borderEdge = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		Border border = BorderFactory.createCompoundBorder(borderBox, borderEdge);

		int componentCount = this.target.getComponentCount();
		for (int i = 0; i < componentCount; i++) {
			JPanel panel = new JPanel() {
				public Dimension getMaximumSize() {
					return new Dimension(super.getMaximumSize().width, this.getPreferredSize().height);
				};
			};
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.setBorder(border);

			Editor<?> editor = this.target.getComponentAtIndex(i).getEditor(panel);
			editor.create();
			
			this.componentListPanel.add(panel);

			if (i != componentCount - 1) { // Don't add a space after the last component.
				this.componentListPanel.add(Box.createVerticalStrut(20));
			}
		}
		
		this.addComponentButton.getParent().revalidate();
	}
}