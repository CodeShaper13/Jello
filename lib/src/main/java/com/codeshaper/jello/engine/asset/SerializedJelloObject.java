package com.codeshaper.jello.engine.asset;

import java.awt.BorderLayout;
import java.nio.file.Path;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.inspector.AssetEditor;
import com.codeshaper.jello.editor.inspector.Editor;
import com.codeshaper.jello.engine.AssetFileExtension;

@AssetFileExtension(SerializedJelloObject.EXTENSION)
public class SerializedJelloObject extends Asset {

	public static final String EXTENSION = "jelobj";
	
	public SerializedJelloObject(Path file) {
		super(file);
	}
	
	@Override
	public Editor<?> getInspectorDrawer() {
		return new SerializedJelloObjectEditor<SerializedJelloObject>(this);
	}
	
	public class SerializedJelloObjectEditor<T extends Asset> extends AssetEditor<T> {

		public SerializedJelloObjectEditor(T target) {
			super(target);
		}

		@Override
		public void drawInspector(JPanel panel) {
			panel.setLayout(new BorderLayout());
			JLabel label = new JLabel("SerializedData default inspector.");			

			panel.add(label, BorderLayout.CENTER);
		}
		
		@Override
		protected void drawHeader(JPanel headerPanel) {
			super.drawHeader(headerPanel);
						
			headerPanel.add(Box.createHorizontalGlue());
			
			JButton saveBtn = new JButton("Save");
			saveBtn.addActionListener(e -> {
				JelloEditor.instance.assetDatabase.saveAsset((SerializedJelloObject) target);
			});
			
			JCheckBox autoSave = new JCheckBox("Auto-Save");
			autoSave.addActionListener(e -> {
				saveBtn.setEnabled(!autoSave.isSelected());
			});
			
			headerPanel.add(autoSave);
			headerPanel.add(saveBtn);
		}
	}
}
