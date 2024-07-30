package com.codeshaper.jello.editor.property.drawer;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.codeshaper.jello.editor.GuiBuilder;
import com.codeshaper.jello.editor.property.IExposedField;

public class BooleanDrawer implements IFieldDrawer {

	@Override
	public JPanel draw(IExposedField field) {
		JCheckBox checkbox = GuiBuilder.checkBox((boolean) field.get(), (v) -> {
			field.set(v);
		});
		checkbox.setEnabled(field.isReadOnly());

		return GuiBuilder.combine(GuiBuilder.label(field), checkbox);
	}
}
