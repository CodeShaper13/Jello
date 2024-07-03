package com.codeshaper.jello.editor.property.drawer;

import javax.swing.JPanel;

import com.codeshaper.jello.editor.GuiUtil;
import com.codeshaper.jello.editor.property.IExposedField;

public class NumberDrawer implements IFieldDrawer {
	
	@Override
	public JPanel draw(IExposedField field) throws Exception {
		return GuiUtil.combine(GuiUtil.label(field), GuiUtil.numberField(field));
	}
}