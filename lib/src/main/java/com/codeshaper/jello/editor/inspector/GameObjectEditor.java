package com.codeshaper.jello.editor.inspector;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.joml.Quaternionf;
import org.joml.Vector3d;

import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.property.ExposedField;
import com.codeshaper.jello.editor.property.drawer.FieldDrawerRegistry;
import com.codeshaper.jello.editor.property.drawer.IFieldDrawer;
import com.codeshaper.jello.editor.window.ComponentPanel;
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

		// GameObject properties:
		this.propertiesPanel = new JPanel(new GridLayout(3, 1));
		
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		gbc.gridy = 1;
		panel.add(this.propertiesPanel, gbc);

		// Component List Header:
		JPanel p2 = new JPanel(new BorderLayout());
		p2.add(new JLabel("Components:"), BorderLayout.WEST);
		this.addComponentButton = new JButton("Add");
		this.addComponentButton.addActionListener(e -> {
			JDialog dialog = new JDialog(JelloEditor.instance.window, "Add Component");
			DefaultListModel<Class<JelloComponent>> model = new  DefaultListModel<Class<JelloComponent>>();
			for(Class<JelloComponent> clazz : JelloEditor.instance.componentList) {
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
		p2.add(this.addComponentButton, BorderLayout.EAST);
		gbc.gridy = 2;
		panel.add(p2, gbc);
		
		p.add(panel, BorderLayout.NORTH);

		// Component List:
		this.componentListPanel = new JPanel();
		this.componentListPanel.setLayout(new BoxLayout(this.componentListPanel, BoxLayout.Y_AXIS));
		this.componentScrollPane = new JScrollPane(this.componentListPanel);
		p.add(this.componentScrollPane, BorderLayout.CENTER);
		
		this.refresh();
	}

	@Override
	public void refresh() {
		super.refresh();
		
		this.componentListPanel.removeAll();

		for (JelloComponent component : this.target.getAllComponents()) {
			ComponentPanel cp = new ComponentPanel(component);
			this.componentListPanel.add(cp);
		}

		this.propertiesPanel.getParent().revalidate();
	}

}
