package com.codeshaper.jello.editor.property.drawer;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.codeshaper.jello.editor.GuiBuilder;
import com.codeshaper.jello.editor.property.IExposedField;

public class EnumDrawer implements IFieldDrawer {

	@Override
	public JPanel draw(IExposedField field) {
		JComboBox<Object> comboBox = new JComboBox<Object>(field.getType().getEnumConstants());
		comboBox.setSelectedItem(field.get());
		comboBox.addActionListener(e -> {
			field.set(comboBox.getSelectedItem());
		});
		
		return GuiBuilder.combine(GuiBuilder.label(field), comboBox);
	}
}
