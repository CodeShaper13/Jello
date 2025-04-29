package com.codeshaper.jello.engine.asset;

import javax.swing.JPanel;

import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.gui.GuiElementButton;
import com.codeshaper.jello.editor.gui.GuiElementToggle;
import com.codeshaper.jello.editor.gui.GuiLayoutBuilder;
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

		private GuiElementButton saveBtn;
		private GuiElementToggle autoSaveToggle;

		public SerializedJelloObjectEditor(T target, JPanel panel) {
			super(target, panel);
		}
		
		@Override
		protected void createHeader(GuiLayoutBuilder builder) {
			super.createHeader(builder);

			this.saveBtn = builder.button("Save", null, () -> {
				this.saveAsset();
			});
			builder.space(5);
			builder.label("Auto-Save");
			builder.space(5);
			this.autoSaveToggle = builder.checkbox(null, JelloEditor.instance.properties.getBoolean(KEY, true), (isOn) -> {
				this.saveBtn.setDisabled(isOn);
				JelloEditor.instance.properties.setBoolean(KEY, isOn);
			});
		}

		@Override
		public void cleanup() {
			super.cleanup();

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
