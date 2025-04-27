package com.codeshaper.jello.editor.property.drawer;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.joml.Quaternionf;

import com.codeshaper.jello.editor.gui.GuiBuilder;
import com.codeshaper.jello.editor.property.IExposedField;

@FieldDrawerType(Quaternionf.class)
public class QuaternionfDrawer extends FieldDrawer {

	@Override
	public JPanel draw(IExposedField field) {
		JComponent quaternionField = GuiBuilder.quaternionField((Quaternionf) field.get(), (v) -> {
			field.set(v);
		});
		
		return GuiBuilder.combine(GuiBuilder.label(field), quaternionField);
	}
}
