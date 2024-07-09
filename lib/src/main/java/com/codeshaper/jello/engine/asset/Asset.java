package com.codeshaper.jello.engine.asset;

import java.nio.file.Path;

import javax.swing.JPanel;

import com.codeshaper.jello.editor.inspector.AssetEditor;
import com.codeshaper.jello.editor.inspector.Editor;
import com.codeshaper.jello.editor.inspector.IInspectable;

public abstract class Asset implements IInspectable {

	/**
	 * The file that provides this asset. May be null if the asset was created at
	 * runtime.
	 */
	public final Path file;
	
	public Asset(Path file) {
		this.file = file;
	}

	public boolean hasProvidingFile() {
		return this.file != null;
	}

	/**
	 * Returns the name of the Asset. If this asset is provided by a file, the files
	 * name is returned. If there is no providing file, null is returned.
	 * 
	 * @return the name of the asset.  May be null.
	 */
	public String getAssetName() {
		if (this.hasProvidingFile()) {
			return this.file.getFileName().toString();
		} else {
			return null;
		}
	}
	
	public void cleanup() { }

	@Override
	public Editor<?> getInspectorDrawer(JPanel panel) {
		return new AssetEditor(this, panel);
	}
}
