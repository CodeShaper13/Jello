package com.codeshaper.jello.editor.property.drawer;

import java.awt.Component;
import java.nio.file.Path;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;

import com.codeshaper.jello.editor.GuiUtil;
import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.property.IExposedField;
import com.codeshaper.jello.engine.asset.Asset;
import com.codeshaper.jello.engine.database.AssetDatabase;

public class AssetDrawer implements IFieldDrawer {

	@Override
	public JPanel draw(IExposedField field) throws Exception {
		AssetDatabase database = JelloEditor.instance.assetDatabase;

		Class<? extends Asset> clazz = (Class<? extends Asset>) field.getType();
		List<Path> paths = database.getAllAssetsOfType(clazz, true);

		JComboBox<Path> comboBox = new JComboBox<Path>();
		comboBox.setRenderer(new ComboBoxRenderer());

		comboBox.addItem(Path.of("None"));
		for (Path path : paths) {
			System.out.println("Path: " + path);
			comboBox.addItem(path);
		}

		Object value = field.get();
		System.out.println(field.getFieldName() + " value =  " + value);
		if (value == null) {
			comboBox.setSelectedIndex(0); // Have None selected.
		} else {
			Path path = ((Asset) value).path;
			if (paths.contains(path)) {
				comboBox.setSelectedItem(((Asset) value).path);
			} else {
				// The selected Asset was made at runtime.
				comboBox.setSelectedIndex(-1);
			}
		}

		comboBox.addActionListener(e -> {
			if (comboBox.getSelectedIndex() == 0) {
				field.set(null);
			} else {
				Path path = (Path) comboBox.getSelectedItem();
				Asset asset = database.getAsset(path);
				System.out.println("Setting: " + asset);
				field.set(asset);
			}
		});

		return GuiUtil.combine(GuiUtil.label(field), comboBox);
	}
	
	private class ComboBoxRenderer extends DefaultListCellRenderer {

	    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus){
	        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

	        if (value == null && index < 0){
	            setText("(Runtime Instance)");
	        }
	        return this;
	    }
	  }
}
