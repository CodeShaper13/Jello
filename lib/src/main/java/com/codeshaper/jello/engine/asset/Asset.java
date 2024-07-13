package com.codeshaper.jello.engine.asset;

import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;

import com.codeshaper.jello.editor.inspector.AssetEditor;
import com.codeshaper.jello.editor.inspector.Editor;
import com.codeshaper.jello.editor.inspector.IInspectable;

public abstract class Asset implements IInspectable {

	/**
	 * The file that provides this asset. Null if the asset was created at runtime.
	 */
	public transient final Path file;

	public Asset(Path file) {
		this.file = file;
	}

	/**
	 * Checks if the Asset came from a file.
	 * 
	 * @return {@code true} if the Asset came from a file, {@code false} if it was
	 *         created at runtime.
	 */
	public boolean hasProvidingFile() {
		return this.file != null;
	}

	/**
	 * Returns the name of the Asset. If this asset is provided by a file, the
	 * file's name without an extension is returned. If there is no providing file, null is returned.
	 * 
	 * @return the name of the asset. May be null.
	 */
	public String getAssetName() {
		if (this.hasProvidingFile()) {
			return FilenameUtils.getBaseName(this.file.getFileName().toString());
		} else {
			return null;
		}
	}

	public void cleanup() {
	}

	@Override
	public Editor<?> getInspectorDrawer() {
		return new AssetEditor<Asset>(this);
	}
}
