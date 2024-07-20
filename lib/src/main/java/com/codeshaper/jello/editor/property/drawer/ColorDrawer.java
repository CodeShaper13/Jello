package com.codeshaper.jello.editor.property.drawer;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JPanel;

import com.codeshaper.jello.editor.GuiBuilder;
import com.codeshaper.jello.editor.property.IExposedField;
import com.codeshaper.jello.engine.Color;

public class ColorDrawer implements IFieldDrawer {

	@Override
	public JPanel draw(IExposedField field) {
		JButton btn = new JButton();
		btn.setBackground(((Color) field.get()).toAwtColor());
				
		JPanel panel = GuiBuilder.combine(GuiBuilder.label(field), btn);
		
		btn.addActionListener(e -> {
			java.awt.Color newColor = JColorChooser.showDialog(panel, "Choose Color", btn.getBackground());
			if(newColor != null) {
				btn.setBackground(newColor);
				field.set(new Color(newColor));
			}
		});

		return panel;
	}

}
