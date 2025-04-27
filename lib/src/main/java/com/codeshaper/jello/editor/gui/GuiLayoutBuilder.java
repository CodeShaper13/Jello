package com.codeshaper.jello.editor.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;
import org.joml.Vector4i;

import com.codeshaper.jello.editor.property.ExposedField;
import com.codeshaper.jello.editor.property.modifier.Button;
import com.codeshaper.jello.editor.property.modifier.DontExposeField;
import com.codeshaper.jello.editor.property.modifier.ExposeField;
import com.codeshaper.jello.editor.property.modifier.Separator;
import com.codeshaper.jello.editor.property.modifier.Space;
import com.codeshaper.jello.editor.swing.JNumberField;
import com.codeshaper.jello.engine.Color;
import com.codeshaper.jello.engine.asset.Asset;

/**
 * Provides an easy way to create Uis with components organized in a descending
 * list. For lower level control, use {@link GuiBuilder}
 */
public final class GuiLayoutBuilder {

	private JPanel panel;
	private JPanel horizontalPanel;

	public GuiLayoutBuilder() {
		this.panel = new JPanel() {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(100, super.getPreferredSize().height);
			}
		};
		this.panel.setLayout(new BoxLayout(this.panel, BoxLayout.Y_AXIS));
	}

	public void clear() {
		this.panel.removeAll();
		this.horizontalPanel = null;
	}
	

	/**
	 * Adds a space to the layout with a size of 14 pixels (the amount of space that
	 * {@link Space} gives.
	 */
	public void space() {
		this.space(10);
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
	
	public void glue() {
		if(this.isHorizontal()) {
			this.add(Box.createHorizontalGlue());
		} else {
			this.add(Box.createVerticalGlue());
		}
	}

	/**
	 * Adds a separator in the layout between the last added component and the next
	 * one to be added.
	 */
	public void separator() {
		int orientation = this.isHorizontal() ? SwingConstants.VERTICAL : SwingConstants.HORIZONTAL;
		this.add(new JSeparator(orientation));
	}

	public JPanel startHorizontal() {
		if (this.isHorizontal()) {
			// Don't do anything, already drawing in horizontal mode.
			return this.horizontalPanel;
		}
		this.horizontalPanel = new JPanel();		
		//this.horizontalPanel.setLayout(new GridLayout(1, 0));
		this.horizontalPanel.setLayout(new BoxLayout(this.horizontalPanel, BoxLayout.X_AXIS));
		
		return this.horizontalPanel;
	}

	public void endHorizontal() {
		if (!this.isHorizontal()) {
			return;
		}

		this.panel.add(this.horizontalPanel);
		this.horizontalPanel = null;
	}

	public boolean isHorizontal() {
		return this.horizontalPanel != null;
	}

	public void setBorder(Border border) {
		this.panel.setBorder(border);
	}

	public void setBorder(String title) {
		this.panel.setBorder(BorderFactory.createTitledBorder(title));
	}

	public GuiLayoutBuilder subPanel(String title) {
		GuiLayoutBuilder builder = new GuiLayoutBuilder();
		builder.panel.setBorder(BorderFactory.createTitledBorder(title));
		this.add(builder.panel);
		return builder;
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
	
	public void image(ImageIcon icon, int width, int height) {
		this.add(GuiBuilder.image(icon, width, height));
	}

	/**
	 * Adds a single line text field where the user can edit a string.
	 * 
	 * @param label
	 * @param text
	 * @param listener
	 */
	public void textField(String label, String text, OnSubmitListerer<String> listener) {
		this.add(this.prefixLabelIfNecessary(label, GuiBuilder.textField(text, listener)));
	}

	/**
	 * Adds a multi-line text field where the user can edit a string.
	 * 
	 * @param label
	 * @param text
	 * @param lines
	 * @param listener
	 */
	public void textArea(String label, String text, int lines, OnSubmitListerer<String> listener) {
		this.add(this.prefixLabelIfNecessary(label, GuiBuilder.textArea(text, lines, listener)));
	}

	public void textBox(String label, String text, int lines) {
		this.add(this.prefixLabelIfNecessary(label, GuiBuilder.textBox(text, lines)));
	}

	/**
	 * Adds a Button to the layout.
	 * 
	 * @param label   the button's label. May be null.
	 * @param icon    the button's icon. May be null.
	 * @param onClick run when the button is clicked. May be null.
	 * @return 
	 */
	public GuiElement button(String label, Icon icon, Runnable onClick) {
		GuiElement element = GuiBuilder.button(label, icon, onClick);
		this.add(element);
		return element;
	}

	/**
	 * Adds a check box to the layout with an optional label in front of it.
	 * 
	 * @param label
	 * @param isOn
	 * @param onClick
	 */
	public void checkbox(String label, boolean isOn, OnSubmitListerer<Boolean> listener) {
		this.add(this.prefixLabelIfNecessary(label, GuiBuilder.checkBox(isOn, listener)));
	}

	/**
	 * Adds an integer field to the layout with an optional label in front of it.
	 * 
	 * @param label    the labels text, or null to hide the label.
	 * @param value    the fields initial value.
	 * @param listener a listener for when the field's value is submitted. May be
	 *                 null.
	 */
	public void intField(String label, int value, OnSubmitListerer<Integer> listener) {
		JNumberField numberField = GuiBuilder.intField(value, listener);
		this.add(this.prefixLabelIfNecessary(label, numberField));
	}

	/**
	 * Adds a long field to the layout with an optional label in front of it.
	 * 
	 * @param label    the labels text, or null to hide the label.
	 * @param value    the fields initial value.
	 * @param listener a listener for when the field's value is submitted. May be
	 *                 null.
	 */
	public void longField(String label, long value, OnSubmitListerer<Long> listener) {
		JNumberField numberField = GuiBuilder.longField(value, listener);
		this.add(this.prefixLabelIfNecessary(label, numberField));
	}

	/**
	 * Adds a float field to the layout with an optional label in front of it.
	 * 
	 * @param label    the labels text, or null to hide the label.
	 * @param value    the fields initial value.
	 * @param listener a listener for when the field's value is submitted. May be
	 *                 null.
	 */
	public void floatField(String label, float value, OnSubmitListerer<Float> listener) {
		JNumberField numberField = GuiBuilder.floatField(value, listener);
		this.add(this.prefixLabelIfNecessary(label, numberField));
	}

	/**
	 * Adds a double field to the layout with an optional label in front of it.
	 * 
	 * @param label    the labels text, or null to hide the label.
	 * @param value    the fields initial value.
	 * @param listener a listener for when the field's value is submitted. May be
	 *                 null.
	 */
	public void doubleField(String label, double value, OnSubmitListerer<Double> listener) {
		JNumberField numberField = GuiBuilder.doubleField(value, listener);
		this.add(this.prefixLabelIfNecessary(label, numberField));
	}

	public void vector2iField(String label, Vector2i value, OnSubmitListerer<Vector2i> listener) {
		this.add(this.prefixLabelIfNecessary(label, GuiBuilder.vector2iField(value, listener)));
	}

	public void vector2fField(String label, Vector2f value, OnSubmitListerer<Vector2f> listener) {
		this.add(this.prefixLabelIfNecessary(label, GuiBuilder.vector2fField(value, listener)));
	}

	public void vector3iField(String label, Vector3i value, OnSubmitListerer<Vector3i> listener) {
		this.add(this.prefixLabelIfNecessary(label, GuiBuilder.vector3iField(value, listener)));
	}

	public void vector3fField(String label, Vector3f value, OnSubmitListerer<Vector3f> listener) {
		this.add(this.prefixLabelIfNecessary(label, GuiBuilder.vector3fField(value, listener)));
	}

	public void vector4iField(String label, Vector4i value, OnSubmitListerer<Vector4i> listener) {
		this.add(this.prefixLabelIfNecessary(label, GuiBuilder.vector4iField(value, listener)));
	}

	public void vector4fField(String label, Vector4f value, OnSubmitListerer<Vector4f> listener) {
		this.add(this.prefixLabelIfNecessary(label, GuiBuilder.vector4fField(value, listener)));
	}

	public void quaternionField(String label, Quaternionf value, OnSubmitListerer<Quaternionf> listener) {
		this.add(this.prefixLabelIfNecessary(label, GuiBuilder.quaternionField(value, listener)));
	}

	public void colorField(String label, Color color, OnSubmitListerer<Color> listener) {
		this.add(this.prefixLabelIfNecessary(label, GuiBuilder.colorField(color, listener)));
	}

	public void doubleSliderField(String label, int min, int max, int value, OnSubmitListerer<Integer> listener) {
		this.add(this.prefixLabelIfNecessary(label, GuiBuilder.intSlider(min, max, value, listener)));
	}

	public void floatSliderField(String label, float min, float max, float value, OnSubmitListerer<Float> listener) {
		this.add(this.prefixLabelIfNecessary(label, GuiBuilder.floatSlider(min, max, value, listener)));
	}

	public void intSliderField(String label, double min, double max, double value, OnSubmitListerer<Double> listener) {
		this.add(this.prefixLabelIfNecessary(label, GuiBuilder.doubleSlider(min, max, value, listener)));
	}

	public <T extends Asset> void assetField(String label, T value, Class<T> clazz, OnSubmitListerer<T> listener) {
		this.add(this.prefixLabelIfNecessary(label, GuiBuilder.assetField(value, clazz, listener)));
	}

	public void field(ExposedField exposedField) {
		JComponent fieldComponent = GuiBuilder.field(exposedField);
		if (fieldComponent != null) {

			Space space = exposedField.getAnnotation(Space.class);
			if (space != null) {
				this.panel.add(Box.createVerticalStrut(space.value()));
			}

			if (exposedField.getAnnotation(Separator.class) != null) {
				this.panel.add(new JSeparator());
			}

			this.panel.add(fieldComponent);
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

	public void add(Component component) {
		if (this.isHorizontal()) {
			this.horizontalPanel.add(component);
		} else {
			if (this.panel.getComponentCount() != 0) {
				this.panel.add(Box.createVerticalStrut(4));
			}
			this.panel.add(component);
		}
	}
	
	public void add(GuiElement element) {
		this.add(element.backingComponent);
	}

	private JComponent prefixLabelIfNecessary(String label, JComponent component) {
		if (label != null) {
			return GuiBuilder.combine(GuiBuilder.label(label), component);
		} else {
			return component;
		}
	}

	public interface OnSubmitListerer<T> {

		void onSubmit(T value);
	}
}
