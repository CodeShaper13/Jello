package com.codeshaper.jello.editor;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.joml.Vector3f;

import com.codeshaper.jello.editor.property.ExposedArrayField;
import com.codeshaper.jello.editor.property.ExposedField;
import com.codeshaper.jello.editor.property.IExposedField;
import com.codeshaper.jello.editor.property.drawer.FieldDrawerRegistry;
import com.codeshaper.jello.editor.property.drawer.IFieldDrawer;
import com.codeshaper.jello.editor.property.modifier.Button;
import com.codeshaper.jello.editor.property.modifier.DontExposeField;
import com.codeshaper.jello.editor.property.modifier.ExposeField;
import com.codeshaper.jello.editor.property.modifier.HideIf;
import com.codeshaper.jello.editor.property.modifier.Separator;
import com.codeshaper.jello.editor.property.modifier.ShowIf;
import com.codeshaper.jello.editor.property.modifier.Space;
import com.codeshaper.jello.editor.swing.JNumberField;
import com.codeshaper.jello.editor.window.InspectorWindow;

/**
 * Provides an easy way to create Uis with components organized in a descending
 * list. For lower level control, use {@link GuiBuilder}
 */
public class GuiLayoutBuilder {

	private JPanel panel;
	private JPanel horizontalPanel;

	public GuiLayoutBuilder() {
		this.panel = new JPanel();
		this.panel.setLayout(new BoxLayout(this.panel, BoxLayout.Y_AXIS));
	}

	/**
	 * Adds a space in the layout.
	 * 
	 * @param size the size of the space in pixels.
	 */
	public void space(int size) {
		if (this.isHorizontal()) {
			this.add(GuiBuilder.horizontalSpace(size));
		} else {
			this.add(GuiBuilder.verticalSpace(size));
		}
	}

	/**
	 * Adds a separator in the layout between the last added component and the next
	 * one to be added.
	 */
	public void separator() {
		int orientation = this.isHorizontal() ? SwingConstants.HORIZONTAL : SwingConstants.VERTICAL;
		this.add(new JSeparator(orientation));
	}

	public void startHorizontal() {
		if (this.isHorizontal()) {
			// Don't do anything, already drawing in horizontal mode.
			return;
		}
		this.horizontalPanel = new JPanel();
		this.horizontalPanel.setLayout(new BoxLayout(this.horizontalPanel, BoxLayout.X_AXIS));

	}

	public void stopHorizontal() {
		if (!this.isHorizontal()) {
			return;
		}

		this.panel.add(this.horizontalPanel);
		this.horizontalPanel = null;
	}

	public boolean isHorizontal() {
		return this.horizontalPanel != null;
	}

	/**
	 * Adds a label to the layout.
	 * 
	 * @param text
	 */
	public void label(String text) {
		this.add(GuiBuilder.label(text));
	}

	/**
	 * Adds a label to the layout.
	 * 
	 * @param text
	 * @param icon
	 * @param alignment
	 */
	public void label(String text, Icon icon, int alignment) {
		this.add(GuiBuilder.label(text, icon, alignment));
	}

	public void textbox(String text, int lines) {
		JTextArea textArea = new JTextArea(lines, 0);
		textArea.setEnabled(false);
		textArea.setText(text);
		JScrollPane scroll = new JScrollPane(textArea);
		this.add(scroll);
	}

	/**
	 * Adds a Button to the layout.
	 * 
	 * @param label   the button's label. May be null.
	 * @param icon    the button's icon. May be null.
	 * @param onClick run when the button is clicked. May be null.
	 */
	public void button(String label, Icon icon, Runnable onClick) {
		JButton btn = GuiBuilder.button(label, icon);
		if (onClick != null) {
			btn.addActionListener(e -> onClick.run());
		}
		this.add(btn);
	}

	/**
	 * Adds a check box to the layout with an optional label in front of it.
	 * 
	 * @param label
	 * @param isOn
	 * @param onClick
	 */
	public void checkbox(String label, boolean isOn, OnSubmitListerer<Boolean> onSubmit) {
		JCheckBox checkBox = GuiBuilder.checkBox(isOn);
		if (onSubmit != null) {
			checkBox.addActionListener((e) -> onSubmit.onSubmit(checkBox.isSelected()));
		}
		this.addLabelIfNecessary(label, checkBox);
	}

	/**
	 * Adds an integer field to the layout with an optional label in front of it.
	 * 
	 * @param label    the labels text, or null to hide the label.
	 * @param value    the fields initial value.
	 * @param onSubmit a listener for when the field's value is submitted. May be
	 *                 null.
	 */
	public void intField(String label, int value, OnSubmitListerer<Integer> onSubmit) {
		JNumberField numberField = GuiBuilder.intField(value);
		this.addNumberFieldListeners(numberField, onSubmit);
		this.addLabelIfNecessary(label, numberField);
	}

	/**
	 * Adds a long field to the layout with an optional label in front of it.
	 * 
	 * @param label    the labels text, or null to hide the label.
	 * @param value    the fields initial value.
	 * @param onSubmit a listener for when the field's value is submitted. May be
	 *                 null.
	 */
	public void longField(String label, long value, OnSubmitListerer<Long> onSubmit) {
		JNumberField numberField = GuiBuilder.longField(value);
		this.addNumberFieldListeners(numberField, onSubmit);
		this.addLabelIfNecessary(label, numberField);
	}

	/**
	 * Adds a float field to the layout with an optional label in front of it.
	 * 
	 * @param label    the labels text, or null to hide the label.
	 * @param value    the fields initial value.
	 * @param onSubmit a listener for when the field's value is submitted. May be
	 *                 null.
	 */
	public void floatField(String label, float value, OnSubmitListerer<Float> onSubmit) {
		JNumberField numberField = GuiBuilder.floatField(value);
		this.addNumberFieldListeners(numberField, onSubmit);
		this.addLabelIfNecessary(label, numberField);
	}

	/**
	 * Adds a double field to the layout with an optional label in front of it.
	 * 
	 * @param label    the labels text, or null to hide the label.
	 * @param value    the fields initial value.
	 * @param onSubmit a listener for when the field's value is submitted. May be
	 *                 null.
	 */
	public void doubleField(String label, double value, OnSubmitListerer<Double> onSubmit) {
		JNumberField numberField = GuiBuilder.doubleField(value);
		this.addNumberFieldListeners(numberField, onSubmit);
		this.addLabelIfNecessary(label, numberField);
	}

	public void vector3Field(String label, Vector3f value, OnSubmitListerer<Vector3f> onSubmit) {
		JPanel horizontalArea = GuiBuilder.horizontalArea();

		JNumberField xField = GuiBuilder.floatField(value.x);
		JNumberField yField = GuiBuilder.floatField(value.y);
		JNumberField zField = GuiBuilder.floatField(value.z);

		horizontalArea.add(new JLabel("X"));
		horizontalArea.add(xField);
		horizontalArea.add(new JLabel("Y"));
		horizontalArea.add(yField);
		horizontalArea.add(new JLabel("Z"));
		horizontalArea.add(zField);

		this.addNumberFieldListeners(xField, (v) -> {
			value.setComponent(0, (float) xField.getValue());
			onSubmit.onSubmit(value);
		});
		this.addNumberFieldListeners(yField, (v) -> {
			value.setComponent(1, (float) yField.getValue());
			onSubmit.onSubmit(value);
		});
		this.addNumberFieldListeners(zField, (v) -> {
			value.setComponent(2, (float) zField.getValue());
			onSubmit.onSubmit(value);
		});

		this.addLabelIfNecessary(label, horizontalArea);
	}

	public void field(ExposedField exposedField) {
		FieldDrawerRegistry drawerRegistry = JelloEditor.instance.filedDrawers;

		HideIf hideIf = exposedField.getAnnotation(HideIf.class);
		if (hideIf != null) {
			try {
				if (EditorUtil.invokeMethod(exposedField, hideIf.value())) {
					return; // Don't draw the field.
				}
			} catch (Exception e) {
			}
		}

		ShowIf showIf = exposedField.getAnnotation(ShowIf.class);
		if (showIf != null) {
			try {
				if (!EditorUtil.invokeMethod(exposedField, showIf.value())) {
					return; // Don't draw the field.
				}
			} catch (Exception e) {
			}
		}

		if (exposedField.getType().isArray()) {
			this.addSpaceIfRequested(panel, exposedField);
			this.addSeparatorIfRequested(panel, exposedField);

			JPanel arrayPanel = GuiBuilder.verticalArea();

			Object arrayInstance = exposedField.get();
			int arrayLength = arrayInstance == null ? 0 : Array.getLength(arrayInstance);

			JNumberField numField = GuiBuilder.intField(arrayLength);
			numField.addActionListener(e -> {
				int newLength = (int) numField.getValue();
				Object newArray = Array.newInstance(exposedField.getType().componentType(), newLength);

				if (arrayInstance != null) {
					System.arraycopy(arrayInstance, 0, newArray, 0, Math.min(arrayLength, newLength));
				}

				exposedField.set(newArray);
				JelloEditor.getWindow(InspectorWindow.class).refresh();
			});

			panel.add(GuiBuilder.combine(GuiBuilder.label(exposedField),
					GuiBuilder.combine(GuiBuilder.label("Length"), numField)));

			if (arrayInstance != null) {
				IFieldDrawer drawer = drawerRegistry.getDrawer(arrayInstance.getClass().getComponentType());
				if (drawer != null) {
					for (int i = 0; i < arrayLength; i++) {
						JPanel fieldPanel;
						try {
							fieldPanel = drawer
									.draw(new ExposedArrayField(exposedField.backingField, arrayInstance, i));
							arrayPanel.add(fieldPanel);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				}
			}

			panel.add(arrayPanel);
		} else {
			IFieldDrawer drawer = drawerRegistry.getDrawer(exposedField.getType());
			if (drawer != null) {
				this.addSpaceIfRequested(panel, exposedField);
				this.addSeparatorIfRequested(panel, exposedField);

				// Draw the field.
				JPanel fieldPanel;
				try {
					fieldPanel = drawer.draw(exposedField);
					panel.add(fieldPanel);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Draws all exposed fields on an object.
	 * 
	 * @param object
	 */
	public void addAll(Object object) {
		Class<?> clazz = object.getClass();

		for (Field field : FieldUtils.getAllFields(clazz)) {
			// Only show public, non static fields.
			int modifiers = field.getModifiers();
			if (Modifier.isStatic(modifiers)) {
				continue;
			}

			boolean hasDontExpose = field.getAnnotation(DontExposeField.class) != null;
			boolean hasExpose = field.getAnnotation(ExposeField.class) != null;

			if (hasDontExpose) {
				continue;
			}

			if (Modifier.isPublic(modifiers) || hasExpose) {
				this.field(new ExposedField(object, field));
			}
		}

		for (Method method : MethodUtils.getMethodsWithAnnotation(clazz, Button.class, true, true)) {
			this.panel.add(GuiBuilder.button(method, object));
		}
	}

	/**
	 * Provides access to the JPanel that is being drawn on for low level access.
	 * 
	 * @return
	 */
	public JPanel getPanel() {
		return this.panel;
	}

	private void add(Component component) {
		if (this.isHorizontal()) {
			this.horizontalPanel.add(component);
		} else {
			this.panel.add(component);
		}
	}

	private void addLabelIfNecessary(String label, JComponent component) {
		if (label != null) {
			this.add(GuiBuilder.combine(GuiBuilder.label(label), component));
		} else {
			this.add(component);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> void addNumberFieldListeners(JNumberField numberField, OnSubmitListerer<T> onSubmit) {
		if (onSubmit != null) {
			numberField.addActionListener((e) -> onSubmit.onSubmit((T) numberField.getValue()));
			numberField.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					EventQueue.invokeLater(() -> {
						onSubmit.onSubmit((T) numberField.getValue());
					});
				}
			});
		}
	}

	/*
	 * Adds a space if the field requests one.
	 * 
	 * @param panel
	 * 
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

	public interface OnSubmitListerer<T> {

		void onSubmit(T value);
	}
}
