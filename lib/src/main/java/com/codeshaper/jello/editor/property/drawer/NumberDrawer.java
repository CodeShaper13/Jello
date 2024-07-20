package com.codeshaper.jello.editor.property.drawer;

import javax.swing.JPanel;

import com.codeshaper.jello.editor.GuiBuilder;
import com.codeshaper.jello.editor.property.IExposedField;

public class NumberDrawer implements IFieldDrawer {
	
	@Override
	public JPanel draw(IExposedField field) {
		return GuiBuilder.combine(GuiBuilder.label(field), GuiBuilder.numberField(field));
	}
}