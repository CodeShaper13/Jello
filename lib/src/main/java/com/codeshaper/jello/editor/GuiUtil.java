package com.codeshaper.jello.editor;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
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
import com.codeshaper.jello.editor.property.modifier.DisplayAs;
import com.codeshaper.jello.editor.property.modifier.MaxValue;
import com.codeshaper.jello.editor.property.modifier.MinValue;
import com.codeshaper.jello.editor.property.modifier.Range;
import com.codeshaper.jello.editor.property.modifier.TextBox;
import com.codeshaper.jello.editor.swing.JNumberField;
import com.codeshaper.jello.editor.swing.JNumberField.EnumNumberType;

@Deprecated
public class GuiUtil {

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

	public static Component verticalSpace(int space) {
		return Box.createVerticalStrut(space);
	}

	public static Component horizontalSpace(int space) {
		return Box.createHorizontalStrut(space);
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

	public static JComponent checkBox(IExposedField field) {
		JCheckBox checkbox = new JCheckBox();
		checkbox.setEnabled(!field.isReadOnly());
		checkbox.setSelected((boolean) field.get());
		checkbox.addActionListener(e -> {
			field.set(checkbox.isSelected());
		});
		return checkbox;
	}

	public static JNumberField numberField(int value) {
		JNumberField numberField = new JNumberField(EnumNumberType.func(int.class));
		numberField.setValue(value);
		return numberField;
	}

	public static JNumberField numberField(float value) {
		JNumberField numberField = new JNumberField(EnumNumberType.func(float.class));
		numberField.setValue(value);
		return numberField;
	}

	public static JNumberField numberField(double value) {
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
		if(min != null) {
			numberField.setMin(min.value());
		}
		MaxValue max = field.getAnnotation(MaxValue.class);
		if(max != null) {
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
