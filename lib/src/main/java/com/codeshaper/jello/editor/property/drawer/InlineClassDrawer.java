package com.codeshaper.jello.editor.property.drawer;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.codeshaper.jello.editor.gui.GuiBuilder;
import com.codeshaper.jello.editor.property.IExposedField;

public abstract class InlineClassDrawer extends FieldDrawer {

	private final String[] fieldNames;
	
	public InlineClassDrawer(String... fieldNames) {
		this.fieldNames = fieldNames;
	}

	@Override
	public JPanel draw(IExposedField field) {
		JPanel horizontalArea = GuiBuilder.horizontalArea();
		boolean isReadOnly = field.isReadOnly();
		
		if(field.get() == null) {
			field.set(this.createDefaultInstance());
		}
		
		for(String s : this.fieldNames) {
			IExposedField subField = field.getSubProperty(s);
			if(subField == null) {
				continue;
			}
			JComponent component = GuiBuilder.field(subField);
			if(component != null) {
				// Makes the labels take up the minimal amount of space.
				if(component instanceof JPanel) {
					JPanel panel = (JPanel)component;
					panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
				}
				horizontalArea.add(component);
				if(isReadOnly || subField.isReadOnly()) {
					component.setEnabled(false);
				}
			}
		}

		return GuiBuilder.combine(GuiBuilder.label(field), horizontalArea);
	}
	
	public abstract Object createDefaultInstance();
}
