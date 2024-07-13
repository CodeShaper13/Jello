package com.codeshaper.jello.editor;

import java.nio.file.Path;

import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.asset.Asset;
import com.codeshaper.jello.engine.asset.SerializedJelloObject;
import com.codeshaper.jello.engine.database.AssetDatabase;

public class EditorAssetDatabase extends AssetDatabase {

	public EditorAssetDatabase(Path projectFolder) {
		super(projectFolder);
	}

	/**
	 * Adds an Asset from the database.
	 * 
	 * @param assetPath the relative path to the asset.
	 * @param asset     the Asset to add to the Database.
	 * @return {@link true} if the Asset was added, {@link false} false if there was
	 *         an error.
	 */
	public boolean addAsset(Path assetPath, Asset asset) {
		if (this.exists(assetPath)) {
			Debug.logError("Could not add Asset, an Asset already exists with the path %s", assetPath);
			return false;
		}

		this.assets.put(this.toFullPath(assetPath), asset);

		return true;
	}

	/**
	 * Removes an Asset from the database.
	 * 
	 * @param assetPath the relative path to the asset.
	 * @return {@link true} if the Asset was removed, {@link false} false if there
	 *         was an error.
	 */
	public boolean removeAsset(Path assetPath) {
		if (!this.exists(assetPath)) {
			Debug.logError("[Asset Database]: Could not remove Asset, no Asset exists with the path %.", assetPath);
			return false;
		}

		this.assets.remove(this.toFullPath(assetPath));
		return true;
	}

	/**
	 * Creates a new Asset in the project. If an Asset already exists at the passed
	 * path, null is returned.
	 * 
	 * @param assetClass the class to instantiate
	 * @param path       the relative or full path of the location to create the
	 *                   asset file.
	 * @param assetName  the name of the asset
	 * @return the new Asset, or null on error.
	 */
	public SerializedJelloObject createAsset(Class<? extends SerializedJelloObject> assetClass, Path path,
			String assetName) {
		Path pathWithNameAndExtension = path.resolve(assetName + "." + SerializedJelloObject.EXTENSION);
		if (this.exists(path)) {
			Debug.logError("[Asset Database]: An Asset already exists at %s", path);
			return null;
		}

		SerializedJelloObject asset = (SerializedJelloObject) this.invokeConstructor(assetClass, this.toFullPath(pathWithNameAndExtension));
		JelloEditor.instance.saveAssetToDisk(asset);
		this.addAsset(pathWithNameAndExtension, asset);

		return asset;
	}
}
