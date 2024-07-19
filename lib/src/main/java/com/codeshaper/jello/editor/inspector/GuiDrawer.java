package com.codeshaper.jello.editor.inspector;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import com.codeshaper.jello.editor.EditorUtil;
import com.codeshaper.jello.editor.GuiUtil;
import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.property.ExposedArrayField;
import com.codeshaper.jello.editor.property.ExposedField;
import com.codeshaper.jello.editor.property.IExposedField;
import com.codeshaper.jello.editor.property.drawer.FieldDrawerRegistry;
import com.codeshaper.jello.editor.property.drawer.IFieldDrawer;
import com.codeshaper.jello.editor.property.modifier.HideIf;
import com.codeshaper.jello.editor.property.modifier.Separator;
import com.codeshaper.jello.editor.property.modifier.ShowIf;
import com.codeshaper.jello.editor.property.modifier.Space;
import com.codeshaper.jello.editor.swing.JNumberField;
import com.codeshaper.jello.editor.swing.JNumberField.EnumNumberType;
import com.codeshaper.jello.editor.window.InspectorWindow;
import com.codeshaper.jello.engine.component.JelloComponent;

public class GuiDrawer {

	private JPanel panel;
	private JPanel horizontalPanel;

	public GuiDrawer() {
		this.panel = new JPanel();
		this.panel.setLayout(new BoxLayout(this.panel, BoxLayout.Y_AXIS));
	}

	public void drawSpace(int size) {
		if (this.isDrawingHorizontal()) {
			this.add(Box.createHorizontalStrut(size));
		} else {
			this.add(Box.createHorizontalStrut(size));
		}
	}

	public void drawSeparator() {
		int orientation = this.isDrawingHorizontal() ? SwingConstants.HORIZONTAL : SwingConstants.VERTICAL;
		this.add(new JSeparator(orientation));
	}

	public void startDrawingHorizontal() {
		if (this.isDrawingHorizontal()) {
			// Don't do anything, already drawing in horizontal mode.
			return;
		}
		this.horizontalPanel = new JPanel();
		this.horizontalPanel.setLayout(new BoxLayout(this.horizontalPanel, BoxLayout.X_AXIS));

	}

	public void stopDrawingHorizontal() {
		if (!this.isDrawingHorizontal()) {
			return;
		}

		this.panel.add(this.horizontalPanel);
		this.horizontalPanel = null;
	}

	public boolean isDrawingHorizontal() {
		return this.horizontalPanel != null;
	}

	public void drawLabel(String text) {
		this.add(new JLabel(text));
	}

	public void drawLabel(String text, Icon icon, int alignment) {
		this.add(new JLabel(text, icon, alignment));
	}

	public void drawTextbox(String text, int lines) {
		JTextArea textArea = new JTextArea(lines, 0);
		textArea.setEnabled(false);
		textArea.setText(text);
		JScrollPane scroll = new JScrollPane(textArea);
		this.add(scroll);
	}

	public void drawButton(String label, Icon icon, Runnable onClick) {
		JButton btn = new JButton(label, icon);
		if(onClick != null) {
			btn.addActionListener(e -> onClick.run());
		}
		this.add(btn);
	}

	public void drawCheckbox(String label, Icon icon, boolean isOn, Runnable onClick) {
		JToggleButton toggle = new JToggleButton(label, icon, isOn);
		if(onClick != null) {
			toggle.addActionListener((e) -> onClick.run());
		}
		this.add(toggle);
	}

	public void drawIntField(int value, OnSubmitListerer<Integer> onSubmit) {
		JNumberField numberField = new JNumberField(EnumNumberType.func(int.class));
		numberField.setValue(value);
		this.func(numberField, onSubmit);
		this.add(numberField);
	}

	public void drawLongField(long value, OnSubmitListerer<Long> onSubmit) {
		JNumberField numberField = new JNumberField(EnumNumberType.func(long.class));
		numberField.setValue(value);
		this.func(numberField, onSubmit);
		this.add(numberField);
	}

	public void drawFloatField(float value, OnSubmitListerer<Float> onSubmit) {
		JNumberField numberField = new JNumberField(EnumNumberType.func(float.class));
		numberField.setValue(value);
		this.func(numberField, onSubmit);
		this.add(numberField);
	}

	public void drawDoubleField(int value, OnSubmitListerer<Double> onSubmit) {
		JNumberField numberField = new JNumberField(EnumNumberType.func(double.class));
		numberField.setValue(value);
		this.func(numberField, onSubmit);
		this.add(numberField);
	}
	
	public void drawField(ExposedField exposedField) {
		FieldDrawerRegistry drawerRegistry = JelloEditor.instance.filedDrawers;

		int modifiers = exposedField.backingField.getModifiers();
		if(Modifier.isStatic(modifiers)) {
			return;
		}
		
		HideIf hideIf = exposedField.getAnnotation(HideIf.class);
		if(hideIf != null) {
			try {
				if (EditorUtil.invokeMethod(exposedField, hideIf.value())) {
					return; // Don't draw the field.
				}
			} catch (Exception e) { }	
		}

		ShowIf showIf = exposedField.getAnnotation(ShowIf.class);
		if(showIf != null) {
			try {
				if (!EditorUtil.invokeMethod(exposedField, showIf.value())) {
					return; // Don't draw the field.
				}
			} catch (Exception e) { }
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

				if (arrayInstance != null) {
					System.arraycopy(arrayInstance, 0, newArray, 0, Math.min(arrayLength, newLength));
				}

				exposedField.set(newArray);
				JelloEditor.getWindow(InspectorWindow.class).refresh();
			});

			panel.add(GuiUtil.combine(GuiUtil.label(exposedField), GuiUtil.combine(GuiUtil.label("Length"), numField)));

			IFieldDrawer drawer = drawerRegistry.getDrawer(arrayInstance.getClass().getComponentType());
			if (drawer != null) {
				for (int i = 0; i < arrayLength; i++) {
					JPanel fieldPanel;
					try {
						fieldPanel = drawer.draw(new ExposedArrayField(exposedField.backingField, arrayInstance, i));
						arrayPanel.add(fieldPanel);
					} catch (Exception e1) {
						e1.printStackTrace();
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
	public void drawObject(Object object) {
		for (Field field : object.getClass().getFields()) {
			// Skip over the fields defined in this class, specifically the isEnabled field.
			if (field.getDeclaringClass() == JelloComponent.class) {
				continue;
			}

			try {
				this.drawField(new ExposedField(object, field));
			} catch (Exception e) {
				e.printStackTrace();
			}
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
		if (this.isDrawingHorizontal()) {
			this.horizontalPanel.add(component);
		} else {
			this.panel.add(component);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> void func(JNumberField numberField, OnSubmitListerer<T> onSubmit) {
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
