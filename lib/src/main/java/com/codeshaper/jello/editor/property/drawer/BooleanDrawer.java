package com.codeshaper.jello.editor.property.drawer;

import javax.swing.JPanel;

import com.codeshaper.jello.editor.GuiUtil;
import com.codeshaper.jello.editor.property.IExposedField;

public class BooleanDrawer implements IFieldDrawer {

	@Override
	public JPanel draw(IExposedField field) throws Exception {
		return GuiUtil.combine(GuiUtil.label(field), GuiUtil.checkBox(field));
	}
}
