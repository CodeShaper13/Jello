package com.codeshaper.jello.editor;

import java.nio.file.Path;

import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.asset.Asset;
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

		System.out.println("Add " + assetPath);
		System.out.println(this.toFullPath(assetPath));
		
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
			Debug.logError("Could not remove Asset, no Asset exists with the path %.", assetPath);
			return false;
		}
		
		System.out.println("Remove " + assetPath);


		this.assets.remove(this.toFullPath(assetPath));
		return true;
	}


}
