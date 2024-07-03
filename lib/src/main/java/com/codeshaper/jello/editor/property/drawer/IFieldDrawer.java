package com.codeshaper.jello.editor.property.drawer;

import javax.swing.JPanel;

import com.codeshaper.jello.editor.property.IExposedField;

public interface IFieldDrawer {

	public JPanel draw(IExposedField field) throws Exception;
}
