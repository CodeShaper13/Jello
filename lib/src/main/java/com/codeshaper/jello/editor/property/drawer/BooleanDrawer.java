package com.codeshaper.jello.editor.property.drawer;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.codeshaper.jello.editor.gui.GuiBuilder;
import com.codeshaper.jello.editor.property.IExposedField;

@FieldDrawerType(boolean.class)
@FieldDrawerType(Boolean.class)
public class BooleanDrawer extends FieldDrawer {

	@Override
	public JPanel draw(IExposedField field) {
		JCheckBox checkbox = GuiBuilder.checkBox((boolean) field.get(), (v) -> {
			field.set(v);
		});
		checkbox.setEnabled(!field.isReadOnly());

		return GuiBuilder.combine(GuiBuilder.label(field), checkbox);
	}
}
