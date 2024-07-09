package com.codeshaper.jello.editor.window;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.codeshaper.jello.editor.EditorUtil;
import com.codeshaper.jello.editor.GuiUtil;
import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.property.ExposedArrayField;
import com.codeshaper.jello.editor.property.ExposedField;
import com.codeshaper.jello.editor.property.IExposedField;
import com.codeshaper.jello.editor.property.drawer.FieldDrawerRegistry;
import com.codeshaper.jello.editor.property.drawer.IFieldDrawer;
import com.codeshaper.jello.editor.property.modifier.Button;
import com.codeshaper.jello.editor.property.modifier.HideIf;
import com.codeshaper.jello.editor.property.modifier.Separator;
import com.codeshaper.jello.editor.property.modifier.ShowIf;
import com.codeshaper.jello.editor.property.modifier.Space;
import com.codeshaper.jello.editor.swing.JNumberField;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.component.JelloComponent;

public class ComponentPanel extends JPanel {

	private static ImageIcon helpIcon = new ImageIcon(ComponentPanel.class.getResource("/editorIcons/component_help.png"));
	private static ImageIcon editIcon = new ImageIcon(ComponentPanel.class.getResource("/editorIcons/component_edit.png"));
	private static ImageIcon removeIcon = new ImageIcon(ComponentPanel.class.getResource("/editorIcons/component_remove.png"));

	private final JelloComponent component;

	public ComponentPanel(JelloComponent component) {
		this.component = component;

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		// this.setBorder(BorderFactory.createCompoundBorder(
		// BorderFactory.createTitledBorder("Player Editor"),
		// BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4),
				BorderFactory.createLineBorder(Color.gray)));

		////////////////////////////
		// Create the header bar. //
		////////////////////////////
		JPanel header = new JPanel(new GridBagLayout());

		JLabel icon = new JLabel("", SwingConstants.LEFT);

		JCheckBox toggle = new JCheckBox();
		toggle.setSelected(component.isEnabled);
		toggle.addActionListener(e -> {
			component.setEnabled(toggle.isSelected());
		});

		JLabel componentName = new JLabel(component.getClass().getSimpleName(), SwingConstants.LEFT);

		JButton btnHelp = new JButton(helpIcon);
		btnHelp.setPreferredSize(new Dimension(24, 24));
		btnHelp.addActionListener(e -> this.openHelpLink());
		btnHelp.setToolTipText("Open Online Help");

		JButton btnEdit = new JButton(editIcon);
		btnEdit.setPreferredSize(new Dimension(24, 24));
		btnEdit.addActionListener(e -> this.editComponent());
		btnEdit.setToolTipText("Edit Component in IDE");

		JButton btnRemove = new JButton(removeIcon);
		btnRemove.setPreferredSize(new Dimension(24, 24));
		btnRemove.addActionListener(e -> this.removeComponent());
		btnRemove.setToolTipText("Remove Component");

		GridBagConstraints labelConstraint = new GridBagConstraints();
		labelConstraint.weightx = 1;

		header.add(icon);
		header.add(toggle);
		header.add(componentName, labelConstraint);
		header.add(btnHelp);
		header.add(btnEdit);
		header.add(btnRemove);
		this.add(header);
		
		this.add(new JSeparator());

		/////////////////////////////////

		this.drawInInspector();
	}
	
	/**
	 * Subclasses should override this and draw onto the panel.
	 * 
	 * @param panel
	 */
	public void drawInInspector() {
		JPanel panel = this;
		
		for (Field field : this.component.getClass().getFields()) {
			// Skip over the fields defined in this class, specifically the isEnabled field.
			if (field.getDeclaringClass() == JelloComponent.class) {
				continue;
			}

			try {
				this.drawField(panel, new ExposedField(this.component, field));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		this.drawButtons(panel);
	}

	protected void drawField(JPanel panel, ExposedField exposedField) throws Exception {
		FieldDrawerRegistry drawerRegistry = JelloEditor.instance.filedDrawers;

		HideIf hideIf = exposedField.getAnnotation(HideIf.class);
		if (hideIf != null && EditorUtil.invokeMethod(exposedField, hideIf.value())) {
			return; // Don't draw the field.
		}

		ShowIf showIf = exposedField.getAnnotation(ShowIf.class);
		if (showIf != null && !EditorUtil.invokeMethod(exposedField, showIf.value())) {
			return; // Don't draw the field.
		}

		if (exposedField.getType().isArray()) {
			this.addSeparatorIfRequested(panel, exposedField);
			this.addSeparatorIfRequested(panel, exposedField);

			JPanel arrayPanel = GuiUtil.verticalArea();

			Object arrayInstance = exposedField.get();			
			int arrayLength = arrayInstance == null ? 0 : Array.getLength(arrayInstance);
			
			JNumberField numField = GuiUtil.numberField(arrayLength);
			numField.addActionListener(e -> {
				int newLength = (int) numField.getValue();
				Object newArray = Array.newInstance(exposedField.getType().componentType(), newLength);
				
				if(arrayInstance != null) {
					System.arraycopy(arrayInstance, 0, newArray, 0, Math.min(arrayLength, newLength));
				}
				
				exposedField.set(newArray);
				JelloEditor.getWindow(InspectorWindow.class).refresh();
			});

			arrayPanel.setBorder(new EmptyBorder(0, 10, 0, 0));

			panel.add(GuiUtil.combine(GuiUtil.label(exposedField), GuiUtil.combine(GuiUtil.label("Length"), numField)));

			for (int i = 0; i < arrayLength; i++) {
				Object element = Array.get(arrayInstance, i);
				
				IFieldDrawer drawer = drawerRegistry.getDrawer(arrayInstance.getClass());
				if (drawer != null) {
					JPanel fieldPanel = drawer.draw(new ExposedArrayField(exposedField.backingField, arrayInstance, i));
					arrayPanel.add(fieldPanel);
				}
			}

			panel.add(arrayPanel);
		} else {
			IFieldDrawer drawer = drawerRegistry.getDrawer(exposedField.getType());
			if (drawer != null) {
				this.addSpaceIfRequested(panel, exposedField);
				this.addSeparatorIfRequested(panel, exposedField);

				// Draw the field.
				JPanel fieldPanel = drawer.draw(exposedField);
				panel.add(fieldPanel);
			}
		}
	}

	protected void drawButtons(JPanel panel) {
		// Draw buttons.
		for (Method method : this.component.getClass().getDeclaredMethods()) {
			Button buttonAnnotation = method.getAnnotation(Button.class);
			if (buttonAnnotation != null) {
				if (method.getParameterCount() != 0) {
					Debug.logError("Methods with the Button annotation must have 0 parameters.");
					continue;
				}

				String buttonText = buttonAnnotation.value();
				JButton btn = new JButton(buttonText.isBlank() ? method.getName() : buttonText);
				btn.addActionListener(e -> {
					try {
						if (method.trySetAccessible()) {
							method.invoke(this.component);
						} else {
							// TODO should a message be logged here?
						}
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
						e1.printStackTrace();
					}
				});
				panel.add(btn);
			}
		}
	}

	/**
	 * Adds a separator if the field requests one.
	 * 
	 * @param panel
	 * @param field
	 */
	private void addSpaceIfRequested(JPanel panel, IExposedField field) {
		Space space = field.getAnnotation(Space.class);
		if (space != null) {
			panel.add(Box.createVerticalStrut(space.value()));
		}
	}

	/**
	 * Adds a separator if the field requests one.
	 * 
	 * @param panel
	 * @param field
	 */
	private void addSeparatorIfRequested(JPanel panel, IExposedField field) {
		if (field.getAnnotation(Separator.class) != null) {
			panel.add(new JSeparator());
		}
	}

	private void openHelpLink() {
		// TODO
		System.out.println("Opening help link...");
	}

	private void editComponent() {
		// TODO
		System.out.println("Opening component in IDE...");
	}

	private void removeComponent() {
		this.component.gameObject.removeComponent(component);
		JelloEditor.getWindow(InspectorWindow.class).refresh();
	}
}
