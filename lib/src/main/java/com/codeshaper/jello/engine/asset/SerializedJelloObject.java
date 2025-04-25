package com.codeshaper.jello.engine.asset;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.inspector.AssetEditor;
import com.codeshaper.jello.editor.inspector.Editor;
import com.codeshaper.jello.engine.AssetFileExtension;
import com.codeshaper.jello.engine.AssetLocation;

@AssetFileExtension(SerializedJelloObject.EXTENSION)
public class SerializedJelloObject extends Asset {

	public static final String EXTENSION = "jelobj";

	public SerializedJelloObject(AssetLocation location) {
		super(location);
	}

	/**
	 * Called right before this Asset is serialized.
	 */
	public void onSerialize() {

	}

	/**
	 * Called immediately after this Asset is deserialized.
	 */
	public void onDeserialize() {

	}

	@Override
	public Editor<?> getEditor(JPanel panel) {
		return new SerializedJelloObjectEditor<SerializedJelloObject>(this, panel);
	}

	public class SerializedJelloObjectEditor<T extends SerializedJelloObject> extends AssetEditor<T> {

		private final String KEY = "auto_save_serialized_jello_object";
		
		private JButton saveBtn;
		private JCheckBox autoSaveToggle;

		public SerializedJelloObjectEditor(T target, JPanel panel) {
			super(target, panel);

			this.saveBtn = new JButton("Save");
			this.saveBtn.addActionListener(e -> {
				this.saveAsset();
			});
			
			this.autoSaveToggle = new JCheckBox("Auto-Save");
			this.autoSaveToggle.setSelected(JelloEditor.instance.properties.getBoolean(KEY, true));
			this.autoSaveToggle.addActionListener(e -> {
				boolean isOn = this.autoSaveToggle.isSelected();
				saveBtn.setEnabled(!isOn);
				JelloEditor.instance.properties.setBoolean(KEY, isOn);
			});
			
			JPanel header = this.header;
			header.add(Box.createHorizontalGlue());
			header.add(this.autoSaveToggle);
			header.add(this.saveBtn);
		}

		@Override
		public void onCleanup() {
			super.onCleanup();

			if(this.autoSaveToggle.isSelected()) {
				this.saveAsset();
			}
		}
		
		/**
		 * Saves the SerializedJelloObject to disk.
		 */
		protected void saveAsset() {
			JelloEditor.instance.assetDatabase.saveAsset(target);

		}
	}
}
