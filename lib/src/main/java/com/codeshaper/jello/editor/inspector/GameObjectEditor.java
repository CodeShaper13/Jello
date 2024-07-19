package com.codeshaper.jello.editor.inspector;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.joml.Quaternionf;
import org.joml.Vector3d;

import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.property.ExposedField;
import com.codeshaper.jello.editor.property.drawer.FieldDrawerRegistry;
import com.codeshaper.jello.editor.property.drawer.IFieldDrawer;
import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.component.JelloComponent;

public class GameObjectEditor extends Editor<GameObject> {

	private JCheckBox enabledToggle;
	private JTextField objectName;
	private JPanel propertiesPanel;
	private JButton addComponentButton;
	private JPanel componentListPanel;
	private JScrollPane componentScrollPane;

	public GameObjectEditor(GameObject target) {
		super(target);
	}

	@Override
	public void draw(JPanel p) {
		super.draw(p);

		p.setLayout(new BorderLayout());

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;

		// Header:
		JPanel headerPanel = new JPanel(new BorderLayout());
		this.enabledToggle = new JCheckBox();
		this.enabledToggle.setEnabled(this.target.isEnabled());
		this.enabledToggle.addActionListener(e -> {
			this.target.setEnabled(this.enabledToggle.isSelected());
		});
		headerPanel.add(this.enabledToggle, BorderLayout.WEST);
		this.objectName = new JTextField();
		this.objectName.setText(this.target.name);
		this.objectName.addActionListener(e -> {
			this.target.name = this.objectName.getText();
		});
		headerPanel.add(this.objectName, BorderLayout.CENTER);
		headerPanel.add(Box.createGlue(), BorderLayout.EAST);
		gbc.gridy = 0;
		panel.add(headerPanel, gbc);

		// GameObject properties
		this.propertiesPanel = new JPanel(new GridLayout(3, 1));
		this.propertiesPanel.setBorder(BorderFactory.createTitledBorder("Transform"));

		FieldDrawerRegistry drawerRegistry = JelloEditor.instance.filedDrawers;
		try {
			IFieldDrawer vecDrawer = drawerRegistry.getDrawer(Vector3d.class);
			IFieldDrawer quatDrawer = drawerRegistry.getDrawer(Quaternionf.class);

			ExposedField posField = new ExposedField(this.target, "localPosition");
			ExposedField rotationField = new ExposedField(this.target, "localRotation");
			ExposedField scaleField = new ExposedField(this.target, "localScale");

			this.propertiesPanel.add(vecDrawer.draw(posField));
			this.propertiesPanel.add(quatDrawer.draw(rotationField));
			this.propertiesPanel.add(vecDrawer.draw(scaleField));
		} catch (Exception e) {
			e.printStackTrace();
		}

		gbc.gridy = 1;
		panel.add(this.propertiesPanel, gbc);

		this.addComponentButton = new JButton("Add Component");
		this.addComponentButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.addComponentButton.addActionListener(e -> {
			JDialog dialog = new JDialog(JelloEditor.instance.window, "Add Component");
			DefaultListModel<Class<JelloComponent>> model = new DefaultListModel<Class<JelloComponent>>();
			for (Class<JelloComponent> clazz : JelloEditor.instance.componentList) {
				model.addElement(clazz);
			}
			JList<Class<JelloComponent>> list = new JList<Class<JelloComponent>>(model);
			list.addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					target.addComponent(list.getSelectedValue());
					dialog.dispose();

					refresh();
				}
			});

			JScrollPane scrollPane = new JScrollPane(list);

			dialog.add(scrollPane);
			dialog.setSize(400, 300);
			dialog.setVisible(true);

		});

		p.add(panel, BorderLayout.NORTH);

		// Component List:
		this.componentListPanel = new JPanel();
		this.componentListPanel.setLayout(new GridBagLayout());
		// this.componentListPanel.setLayout(new BoxLayout(this.componentListPanel,
		// BoxLayout.Y_AXIS));
		this.componentScrollPane = new JScrollPane(this.componentListPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.componentScrollPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Components"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		p.add(this.componentScrollPane, BorderLayout.CENTER);

		p.add(this.addComponentButton, BorderLayout.SOUTH);

		this.refresh();
	}

	@Override
	public void refresh() {
		super.refresh();

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

		this.propertiesPanel.getParent().revalidate();
	}
}