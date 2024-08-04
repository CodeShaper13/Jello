package com.codeshaper.jello.editor.property.drawer;

import java.awt.EventQueue;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.codeshaper.jello.editor.GuiBuilder;
import com.codeshaper.jello.editor.property.IExposedField;
import com.codeshaper.jello.editor.property.modifier.TextBox;

@FieldDrawerType(String.class)
public class StringDrawer extends FieldDrawer {

	@Override
	public JPanel draw(IExposedField field) {		
		return GuiBuilder.combine(GuiBuilder.label(field), this.getTextComponent(field));
	}
	
	private JComponent getTextComponent(IExposedField field) {
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
}
