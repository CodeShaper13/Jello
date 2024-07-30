package com.codeshaper.jello.editor.property.drawer;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.codeshaper.jello.editor.GuiBuilder;
import com.codeshaper.jello.editor.property.IExposedField;
import com.codeshaper.jello.engine.Color;

public class ColorDrawer implements IFieldDrawer {

	@Override
	public JPanel draw(IExposedField field) {
		JButton colorField = GuiBuilder.colorField((Color) field.get(), (v) -> {
			field.set(v);

		});

		return GuiBuilder.combine(GuiBuilder.label(field), colorField);
	}
}
