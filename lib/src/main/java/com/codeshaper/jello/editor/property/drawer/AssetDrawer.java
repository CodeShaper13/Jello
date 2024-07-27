package com.codeshaper.jello.editor.property.drawer;

import java.awt.Component;
import java.nio.file.Path;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;

import com.codeshaper.jello.editor.GuiBuilder;
import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.property.IExposedField;
import com.codeshaper.jello.engine.AssetLocation;
import com.codeshaper.jello.engine.asset.Asset;
import com.codeshaper.jello.engine.database.AssetDatabase;

public class AssetDrawer implements IFieldDrawer {

	@SuppressWarnings("unchecked")
	@Override
	public JPanel draw(IExposedField field) {
		AssetDatabase database = JelloEditor.instance.assetDatabase;

		Class<? extends Asset> clazz = (Class<? extends Asset>) field.getType();
		List<Path> paths = database.getAllAssetsOfType(clazz, true);

		JComboBox<Path> comboBox = new JComboBox<Path>();
		comboBox.setRenderer(new ComboBoxRenderer(field));

		comboBox.addItem(Path.of("None"));
		for (Path path : paths) {
			comboBox.addItem(path);
		}

		Object value = field.get();
		if (value == null) {
			comboBox.setSelectedIndex(0); // Have None selected.
		} else {
			AssetLocation location = ((Asset)value).location;
			if (location != null && paths.contains(location.getPath())) {
				comboBox.setSelectedItem(location.getPath());
			} else {
				comboBox.setSelectedIndex(-1);
			}
		}

		comboBox.addActionListener(e -> {
			if (comboBox.getSelectedIndex() == 0) {
				field.set(null);
			} else {
				Path path = (Path) comboBox.getSelectedItem();
				Asset asset = database.getAsset(path);
				field.set(asset);
			}
		});

		return GuiBuilder.combine(GuiBuilder.label(field), comboBox);
	}

	private class ComboBoxRenderer extends DefaultListCellRenderer {

		private final IExposedField field;
		
		public ComboBoxRenderer(IExposedField field) {
			this.field = field;
		}

		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if(value == null && index == -1) {
				Asset asset = (Asset)field.get();
				if(asset.isRuntimeAsset()) {
					this.setText("(Runtime Instance)");
				} else {
					this.setText("(Missing)");
				}
			}
			
			return this;
		}
	}
}
