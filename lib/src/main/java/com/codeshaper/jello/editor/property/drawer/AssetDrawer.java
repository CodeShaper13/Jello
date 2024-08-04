package com.codeshaper.jello.editor.property.drawer;

import java.nio.file.Path;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.codeshaper.jello.editor.GuiBuilder;
import com.codeshaper.jello.editor.property.IExposedField;
import com.codeshaper.jello.engine.asset.Asset;

@FieldDrawerType(Asset.class)
public class AssetDrawer extends FieldDrawer {

	@SuppressWarnings("unchecked")
	@Override
	public JPanel draw(IExposedField field) {
		JComboBox<Path> comboBox = GuiBuilder.assetField(
				(Asset)field.get(),
				(Class<Asset>) field.getType(),
				(v) -> {
			field.set(v);
		});
		comboBox.setEnabled(!field.isReadOnly());
		
		return GuiBuilder.combine(GuiBuilder.label(field), comboBox);
	}
}
