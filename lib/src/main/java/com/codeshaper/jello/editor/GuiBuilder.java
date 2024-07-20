package com.codeshaper.jello.editor;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;

import com.codeshaper.jello.editor.property.IExposedField;
import com.codeshaper.jello.editor.property.modifier.Button;
import com.codeshaper.jello.editor.property.modifier.DisplayAs;
import com.codeshaper.jello.editor.property.modifier.MaxValue;
import com.codeshaper.jello.editor.property.modifier.MinValue;
import com.codeshaper.jello.editor.property.modifier.Range;
import com.codeshaper.jello.editor.property.modifier.TextBox;
import com.codeshaper.jello.editor.swing.JNumberField;
import com.codeshaper.jello.editor.swing.JNumberField.EnumNumberType;
import com.codeshaper.jello.engine.Debug;

/**
 * Provides a collection of static methods that create components for UIs.
 * <p>
 * This is a lower level alternative to using {@link GuiLayoutBuilder}.
 */
public class GuiBuilder {

	/**
	 * Combines 2 or more components into the same horizontal space. All components
	 * receive equal space.
	 * 
	 * @param components
	 * @return a {@link JPanel} holding the components.
	 */
	public static JPanel combine(JComponent... components) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, components.length));
		for (JComponent component : components) {
			panel.add(component);
		}
		return panel;
	}

	public static JPanel horizontalArea(JComponent... components) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		for (JComponent component : components) {
			panel.add(component);
		}

		return panel;
	}

	public static JPanel verticalArea(JComponent... components) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		for (JComponent component : components) {
			panel.add(component);
		}

		return panel;
	}

	public static Component verticalSpace(int size) {
		return Box.createVerticalStrut(size);
	}

	public static Component horizontalSpace(int size) {
		return Box.createHorizontalStrut(size);
	}

	public static JSeparator separator() {
		return new JSeparator();
	}

	public static JComponent textbox(String text, int lines) {
		JTextArea textArea = new JTextArea(lines, 0);
		textArea.setEnabled(false);
		textArea.setText(text);
		JScrollPane scroll = new JScrollPane(textArea);
		return scroll;
	}

	public static JLabel label(String label) {
		return new JLabel(label);
	}

	public static JLabel label(String label, Icon icon, int alignment) {
		return new JLabel(label, icon, alignment);
	}

	public static JLabel label(IExposedField field) {
		DisplayAs dispalyAs = field.getAnnotation(DisplayAs.class);
		return new JLabel(dispalyAs != null ? dispalyAs.value() : StringUtils.capitalize(field.getFieldName()));
	}

	public static JComponent textField(IExposedField field) {
		TextBox annotation = field.getAnnotation(TextBox.class);
		if (annotation == null) {
			JTextField textField = new JTextField();
			textField.addActionListener(e -> {
				field.set(textField.getText());
			});
			textField.setEnabled(!field.isReadOnly());
			textField.setText((String) field.get());
			return textField;
		} else {
			int lines = annotation.value();
			JTextArea textArea = new JTextArea(lines, 0);
			JScrollPane scroll = new JScrollPane(textArea);
			textArea.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							field.set(textArea.getText());
						}
					});
				}
			});
			textArea.setEnabled(!field.isReadOnly());
			textArea.setText((String) field.get());
			return scroll;
		}
	}

	public static JButton button(String label, Icon icon) {
		return new JButton(label, icon);
	}

	public static JButton button(Method method, Object instance) {
		String label = "";

		Button buttonAnnotation = method.getAnnotation(Button.class);
		if (buttonAnnotation != null) {
			label = buttonAnnotation.value();
		}

		JButton button = new JButton(StringUtils.isWhitespace(label) ? method.getName() : label);
		button.addActionListener((e) -> {
			try {
				method.setAccessible(true);
				method.invoke(instance);
			} catch (SecurityException | IllegalAccessException exception) {
				Debug.log("Button's method can not be accessed.  Try making it public.");
				exception.printStackTrace();
			} catch (IllegalArgumentException exception) {
				Debug.logError("Button's method must have no parameters.");
				exception.printStackTrace();
			} catch (InvocationTargetException exception) {
				Debug.logError("Button's method threw %s exception", exception.getCause().toString());
				exception.printStackTrace();
			}
		});

		return button;
	}

	public static JCheckBox checkBox(boolean isOn) {
		JCheckBox checkBox = new JCheckBox();
		checkBox.setSelected(isOn);
		return checkBox;
	}

	public static JCheckBox checkBox(IExposedField field) {
		JCheckBox checkbox = new JCheckBox();
		checkbox.setEnabled(!field.isReadOnly());
		checkbox.setSelected((boolean) field.get());
		checkbox.addActionListener(e -> {
			field.set(checkbox.isSelected());
		});
		return checkbox;
	}

	public static JNumberField intField(int value) {
		JNumberField numberField = new JNumberField(EnumNumberType.func(int.class));
		numberField.setValue(value);
		return numberField;
	}

	public static JNumberField longField(long value) {
		JNumberField numberField = new JNumberField(EnumNumberType.func(long.class));
		numberField.setValue(value);
		return numberField;
	}

	public static JNumberField floatField(float value) {
		JNumberField numberField = new JNumberField(EnumNumberType.func(float.class));
		numberField.setValue(value);
		return numberField;
	}

	public static JNumberField doubleField(double value) {
		JNumberField numberField = new JNumberField(EnumNumberType.func(double.class));
		numberField.setValue(value);
		return numberField;
	}

	public static JNumberField numberField(IExposedField field) {
		Range annotaiton = field.getAnnotation(Range.class);
		if (annotaiton == null) {
			// TODO
		} else {
			// TODO
		}

		JNumberField numberField = new JNumberField(EnumNumberType.func(field.getType()));
		MinValue min = field.getAnnotation(MinValue.class);
		if (min != null) {
			numberField.setMin(min.value());
		}
		MaxValue max = field.getAnnotation(MaxValue.class);
		if (max != null) {
			numberField.setMax(max.value());
		}
		numberField.setEnabled(!field.isReadOnly());
		numberField.setValue(field.get());
		numberField.addActionListener(e -> {
			field.set(numberField.getValue());
		});
		numberField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						field.set(numberField.getValue());
					}
				});
			}
		});
		return numberField;
	}
}
