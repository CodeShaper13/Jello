package com.codeshaper.jello.editor.inspector;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang3.StringUtils;

import com.codeshaper.jello.editor.GuiLayoutBuilder;
import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.property.ExposedField;
import com.codeshaper.jello.engine.ComponentName;
import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.JelloComponent;

public class GameObjectEditor extends Editor<GameObject> {

	private JCheckBox enabledToggle;
	private JTextField objectName;
	private JButton addComponentButton;
	private JPanel componentListPanel;
	private JScrollPane componentScrollPane;

	public GameObjectEditor(GameObject target, JPanel panel) {
		super(target, panel);

		panel.setLayout(new BorderLayout());

		JPanel upperPanel = new JPanel();
		upperPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;

		// Header:
		gbc.gridy = 0;
		upperPanel.add(this.createHeaderPanel(), gbc);

		// Transform Panel.
		gbc.gridy = 1;
		upperPanel.add(this.createTransformPanel(), gbc);

		this.addComponentButton = new JButton("Add Component");
		this.addComponentButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.addComponentButton.addActionListener(e -> {
			JDialog dialog = new AddComponentDialog();
			dialog.setVisible(true);

		});

		// Component List:
		this.componentListPanel = new JPanel();
		this.componentListPanel.setLayout(new GridBagLayout());

		this.componentScrollPane = new JScrollPane(this.componentListPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.componentScrollPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Components"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		panel.add(upperPanel, BorderLayout.NORTH);
		panel.add(this.componentScrollPane, BorderLayout.CENTER);
		panel.add(this.addComponentButton, BorderLayout.SOUTH);

		this.createComponentListPanel();
	}

	@Override
	public void onRefresh() {
		super.onRefresh();
		
		this.enabledToggle.setSelected(this.target.isActive());
		this.objectName.setText(this.target.getName());
		
		this.createComponentListPanel();
	}
	
	private void createComponentListPanel() {
		this.componentListPanel.removeAll();

		GridBagConstraints constraint = new GridBagConstraints();
		constraint.fill = GridBagConstraints.BOTH;
		constraint.weightx = 1.0f;

		Border borderRightSpace = BorderFactory.createEmptyBorder(0, 0, 0, 8);
		Border borderBox = BorderFactory.createTitledBorder("");
		Border borderEdge = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		Border border = BorderFactory.createCompoundBorder(borderRightSpace,
				BorderFactory.createCompoundBorder(borderBox, borderEdge));

		int componentCount = this.target.getComponentCount();
		for (int i = 0; i < componentCount; i++) {
			JelloComponent component = this.target.getComponentAtIndex(i);
			ComponentDrawer<? extends JelloComponent> editor = component.getComponentDrawer();
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

			panel.setBorder(border);
			editor.makeGui(panel);
			constraint.gridy = i * 2;
			this.componentListPanel.add(panel, constraint);

			if (i != componentCount - 1) { // Don't add a space after the last component.
				constraint.gridy = (i * 2) + 1;
				this.componentListPanel.add(Box.createVerticalStrut(20), constraint);
			}
		}

		this.addComponentButton.getParent().revalidate();
	}

	private JPanel createHeaderPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		this.enabledToggle = new JCheckBox();
		this.enabledToggle.setSelected(this.target.isActive());
		this.enabledToggle.addActionListener(e -> {
			this.target.setActive(this.enabledToggle.isSelected());
		});
		panel.add(this.enabledToggle, BorderLayout.WEST);
		this.objectName = new JTextField();
		this.objectName.setText(this.target.getName());
		this.objectName.addActionListener(e -> {
			this.target.setName(this.objectName.getText());
		});
		panel.add(this.objectName, BorderLayout.CENTER);
		panel.add(Box.createGlue(), BorderLayout.EAST);

		return panel;
	}

	private JPanel createTransformPanel() {
		GuiLayoutBuilder builder = new GuiLayoutBuilder();

		builder.vector3fField("Position", this.target.getLocalPosition(), (v) -> {
			this.target.setLocalPosition(v);
		});
		builder.field(new ExposedField(this.target, "localRotation"));
		builder.vector3fField("Scale", this.target.getLocalScale(), (v) -> {
			this.target.setLocalScale(v);
		});

		JPanel panel = builder.getPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Transform"));

		return panel;
	}

	private class AddComponentDialog extends JDialog {
		
		public AddComponentDialog() {
			super(JelloEditor.instance.window, "Add Component");
			
			DefaultListModel<Class<JelloComponent>> model = new DefaultListModel<Class<JelloComponent>>();
			for (Class<JelloComponent> clazz : JelloEditor.instance.assetDatabase.getallComponents()) {
				model.addElement(clazz);
			}
			JList<Class<JelloComponent>> list = new JList<Class<JelloComponent>>(model);
			list.addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					target.addComponent(list.getSelectedValue());
					dispose();
					createComponentListPanel();
				}
			});
			list.setCellRenderer(new CellRenderer());

			JScrollPane scrollPane = new JScrollPane(list);
			this.add(scrollPane);

			this.setSize(400, 300);
		}
		
		private class CellRenderer extends JLabel implements ListCellRenderer<Class<JelloComponent>> {

			@Override
			public Component getListCellRendererComponent(JList<? extends Class<JelloComponent>> list,
					Class<JelloComponent> value, int index, boolean isSelected, boolean cellHasFocus) {
				ComponentName annotation = value.getAnnotation(ComponentName.class);
				if(annotation != null && !StringUtils.isWhitespace(annotation.value())) {
					this.setText(annotation.value());
				} else {
					this.setText(value.getName());
				}
				
				return this;
			}			
		}
	}
}