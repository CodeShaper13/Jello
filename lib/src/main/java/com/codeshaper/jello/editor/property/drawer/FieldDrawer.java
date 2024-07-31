package com.codeshaper.jello.editor.property.drawer;

import javax.swing.JPanel;

import com.codeshaper.jello.editor.property.IExposedField;

public abstract class FieldDrawer {

	public abstract JPanel draw(IExposedField field);
}
