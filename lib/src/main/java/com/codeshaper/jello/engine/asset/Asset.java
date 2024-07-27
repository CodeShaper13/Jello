package com.codeshaper.jello.engine.asset;

import org.apache.commons.io.FilenameUtils;

import com.codeshaper.jello.editor.inspector.AssetEditor;
import com.codeshaper.jello.editor.inspector.Editor;
import com.codeshaper.jello.editor.inspector.IInspectable;
import com.codeshaper.jello.engine.AssetLocation;

public abstract class Asset implements IInspectable {

	/**
	 * The location of the file that provides this Asset. For Asset's created at
	 * runtime (e.g. a dynamic {@link Mesh}) this will be null.
	 */
	public final transient AssetLocation location;

	public Asset(AssetLocation location) {
		this.location = location;
	}

	/**
	 * Checks if the Asset created at runtime, thus does not have a providing file.
	 * 
	 * @return {@code true} if the Asset was created at runtime, {@code false} if it
	 *         came from a file.
	 */
	public boolean isRuntimeAsset() {
		return this.location == null;
	}

	/**
	 * Returns the name of the Asset. If this asset is provided by a file, the
	 * file's name without an extension is returned. If there is no providing file,
	 * null is returned.
	 * 
	 * @return the name of the asset. May be null.
	 */
	public String getAssetName() {
		if (this.isRuntimeAsset()) {
			return null;
		} else {
			return FilenameUtils.getBaseName(this.location.getName());
		}
	}

	public void cleanup() {
	}

	@Override
	public Editor<?> getInspectorDrawer() {
		return new AssetEditor<Asset>(this);
	}
}
