package com.codeshaper.jello.editor.property.drawer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.codeshaper.jello.editor.GuiBuilder;
import com.codeshaper.jello.editor.property.IExposedField;
import com.codeshaper.jello.editor.swing.JNumberField;
import com.codeshaper.jello.engine.MathHelper;

@FieldDrawerType(Quaternionf.class)
public class QuaternionfDrawer extends FieldDrawer {

	@Override
	public JPanel draw(IExposedField field) {
		JPanel horizontalArea = GuiBuilder.horizontalArea();
		
		Vector3f eu = MathHelper.quaternionToEulerAnglesDegrees((Quaternionf)field.get());
		
		JNumberField xField = GuiBuilder.floatField(eu.x, null);
		JNumberField yField = GuiBuilder.floatField(eu.y, null);
		JNumberField zField = GuiBuilder.floatField(eu.z, null);
		
		ActionListener listener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Vector3f eulerDegrees = new Vector3f((float)xField.getValue(), (float)yField.getValue(), (float)zField.getValue());
				field.set(MathHelper.quaternionFromEulerAnglesDegrees(eulerDegrees));
			}
		};
		
		xField.addActionListener(listener);
		yField.addActionListener(listener);		
		zField.addActionListener(listener);
		
		horizontalArea.add(GuiBuilder.label("X"));
		horizontalArea.add(xField);
		horizontalArea.add(GuiBuilder.label("Y"));
		horizontalArea.add(yField);
		horizontalArea.add(GuiBuilder.label("Z"));
		horizontalArea.add(zField);

		return GuiBuilder.combine(GuiBuilder.label(field), horizontalArea);
	}
}
