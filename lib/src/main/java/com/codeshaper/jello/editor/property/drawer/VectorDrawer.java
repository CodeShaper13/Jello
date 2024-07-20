package com.codeshaper.jello.editor.property.drawer;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.lang3.StringUtils;

import com.codeshaper.jello.editor.GuiBuilder;
import com.codeshaper.jello.editor.property.IExposedField;

public class VectorDrawer implements IFieldDrawer {

	private final String[] fieldNames;
	
	public VectorDrawer(String... fieldNames) {
		this.fieldNames = fieldNames;
	}

	@Override
	public JPanel draw(IExposedField field) {
		JPanel horizontalArea = GuiBuilder.horizontalArea();
		
		for(String s : this.fieldNames) {
			horizontalArea.add(new JLabel(StringUtils.capitalize(s)));
			horizontalArea.add(GuiBuilder.numberField(field.getSubProperty(s)));
		}

		return GuiBuilder.combine(GuiBuilder.label(field), horizontalArea);
	}
}
