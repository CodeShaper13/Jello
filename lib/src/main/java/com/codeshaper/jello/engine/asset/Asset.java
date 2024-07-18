package com.codeshaper.jello.engine.asset;

import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;

import com.codeshaper.jello.editor.EditorAssetDatabase;
import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.inspector.AssetEditor;
import com.codeshaper.jello.editor.inspector.Editor;
import com.codeshaper.jello.editor.inspector.IInspectable;
import com.codeshaper.jello.engine.database.AssetDatabase;

public abstract class Asset implements IInspectable {

	/**
	 * A relative path to the file that provides this asset. Null if the asset was created at runtime.
	 * <p>
	 * This should NEVER be changed, instead use
	 * {@link EditorAssetDatabase#renameAsset(Path, String)} if you want to move the
	 * asset. If the Asset is moved at any point, this field will be updated
	 * accordingly by the AssetDatabase.
	 */
	public transient Path path;

	public Asset(Path path) {
		this.path = path;
	}

	/**
	 * Checks if the Asset came from a file.
	 * 
	 * @return {@code true} if the Asset came from a file, {@code false} if it was
	 *         created at runtime.
	 */
	public boolean hasProvidingFile() {
		return this.path != null;
	}
	
	public Path getFullPath() {
		return JelloEditor.instance.assetDatabase.toFullPath(this.path);
	}

	/**
	 * Returns the name of the Asset. If this asset is provided by a file, the
	 * file's name without an extension is returned. If there is no providing file,
	 * null is returned.
	 * 
	 * @return the name of the asset. May be null.
	 */
	public String getAssetName() {
		if (this.hasProvidingFile()) {
			return FilenameUtils.getBaseName(this.path.toString());
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
