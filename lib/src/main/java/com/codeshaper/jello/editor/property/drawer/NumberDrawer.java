package com.codeshaper.jello.editor.property.drawer;

import javax.swing.JPanel;

import com.codeshaper.jello.editor.GuiBuilder;
import com.codeshaper.jello.editor.property.IExposedField;

@FieldDrawerType(byte.class)
@FieldDrawerType(Byte.class)
@FieldDrawerType(short.class)
@FieldDrawerType(Short.class)
@FieldDrawerType(int.class)
@FieldDrawerType(Integer.class)
@FieldDrawerType(long.class)
@FieldDrawerType(Long.class)
@FieldDrawerType(float.class)
@FieldDrawerType(Float.class)
@FieldDrawerType(double.class)
@FieldDrawerType(Double.class)
public class NumberDrawer extends FieldDrawer {
	
	@Override
	public JPanel draw(IExposedField field) {
		return GuiBuilder.combine(GuiBuilder.label(field), GuiBuilder.numberField(field));
	}
}