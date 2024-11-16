package com.codeshaper.jello.editor.property.drawer;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.codeshaper.jello.editor.GuiBuilder;
import com.codeshaper.jello.editor.property.IExposedField;
import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.GameObjectReference;

@FieldDrawerType(GameObjectReference.class)
public class GameObjectReferenceDrawer extends FieldDrawer {

	@Override
	public JPanel draw(IExposedField field) {		
		if(field.get() == null) {
			field.set(new GameObjectReference());
		}
		
		GameObjectReference ref = (GameObjectReference)field.get();
		
		JComboBox<GameObject> comboBox = GuiBuilder.gameObjectField(
				ref.get(),
				(v) -> {
					ref.set(v);
		});
		comboBox.setEnabled(!field.isReadOnly());
		
		return GuiBuilder.combine(GuiBuilder.label(field), comboBox);
	}
}
