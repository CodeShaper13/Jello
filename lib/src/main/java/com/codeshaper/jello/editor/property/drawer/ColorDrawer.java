package com.codeshaper.jello.editor.property.drawer;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.codeshaper.jello.editor.GuiBuilder;
import com.codeshaper.jello.editor.property.IExposedField;
import com.codeshaper.jello.engine.Color;

@FieldDrawerType(Color.class)
public class ColorDrawer extends FieldDrawer {

	@Override
	public JPanel draw(IExposedField field) {
		JButton colorField = GuiBuilder.colorField((Color) field.get(), (v) -> {
			field.set(v);
		});
		colorField.setEnabled(!field.isReadOnly());

		return GuiBuilder.combine(GuiBuilder.label(field), colorField);
	}
}
