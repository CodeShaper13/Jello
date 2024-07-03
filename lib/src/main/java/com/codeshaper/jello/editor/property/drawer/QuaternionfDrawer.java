package com.codeshaper.jello.editor.property.drawer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.codeshaper.jello.editor.GuiUtil;
import com.codeshaper.jello.editor.property.IExposedField;
import com.codeshaper.jello.editor.swing.JNumberField;
import com.codeshaper.jello.engine.MathHelper;

public class QuaternionfDrawer implements IFieldDrawer {

	@Override
	public JPanel draw(IExposedField field) throws Exception {
		JPanel horizontalArea = GuiUtil.horizontalArea();
		
		Vector3f eu = MathHelper.quaternionToEulerAnglesDegrees((Quaternionf)field.get());
		
		JNumberField xField = GuiUtil.numberField(eu.x);
		JNumberField yField = GuiUtil.numberField(eu.y);
		JNumberField zField = GuiUtil.numberField(eu.z);
		
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
		
		horizontalArea.add(GuiUtil.label("X"));
		horizontalArea.add(xField);
		horizontalArea.add(GuiUtil.label("Y"));
		horizontalArea.add(yField);
		horizontalArea.add(GuiUtil.label("Z"));
		horizontalArea.add(zField);

		return GuiUtil.combine(GuiUtil.label(field), horizontalArea);
	}
}
