package com.codeshaper.jello.editor;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import com.codeshaper.jello.editor.event.ProjectReloadListener;
import com.codeshaper.jello.editor.event.ProjectReloadListener.Phase;
import com.codeshaper.jello.editor.scripts.ScriptCompiler;
import com.codeshaper.jello.editor.utils.JelloFileUtils;
import com.codeshaper.jello.engine.AssetLocation;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.SceneManager;
import com.codeshaper.jello.engine.asset.Asset;
import com.codeshaper.jello.engine.asset.Script;
import com.codeshaper.jello.engine.asset.SerializedJelloObject;
import com.codeshaper.jello.engine.database.AssetDatabase;
import com.codeshaper.jello.engine.database.CachedAsset;
import com.codeshaper.jello.engine.database.ComponentList;

public class EditorAssetDatabase extends AssetDatabase {

	private final ScriptCompiler compiler;

	public EditorAssetDatabase(Path projectFolder) {
		super(projectFolder);

		this.compiler = new ScriptCompiler(JelloEditor.instance.rootProjectFolder.toFile(), this);
	}

	/**
	 * Rebuilds the Asset Database. A rebuild consists of:
	 * <li>{@link ProjectReloadListener} with the {@link Phase#PRE_REBUILD} phase is
	 * fired.
	 * <li>Assets file that no longer exist in the project will have their
	 * corresponding {@link Asset} instance removed from the database.
	 * <li>All {@link Script}s will be compiled.
	 * <li>{@link ExtensionMapping} will update it's File Extension to
	 * {@link Class}<{@link Asset}> mapping.
	 * <li>{@link ComponentList} will update it's list of components.
	 * <li>New Files in the /assets folder will have their corresponding
	 * {@link Asset} instance created.
	 * <li>Assets that have had their providing file changed since they were last
	 * imported will be re-imported.
	 * <li>{@link ProjectReloadListener} with the {@link Phase#POST_REBUILD} phase
	 * is fired.
	 */
	public void rebuild() {
		boolean verboseMode = true;

		Debug.log("[Editor] Building Database...");

		this.invokeEvent(Phase.PRE_REBUILD);

		SceneManager sceneManager = JelloEditor.instance.sceneManager;
		SceneManagerSnapshot snapshot = new SceneManagerSnapshot(sceneManager);

		// Check all the Assets and if the file that provides them no longer exists,
		// remove the Asset from the Database.
		for (int i = this.assets.size() - 1; i >= 0; --i) {
			CachedAsset asset = this.assets.get(i);
			if (asset.isBuiltin()) {
				continue; // builtin assets are never removed.
			}
			File file = asset.location.getFile();
			if (!file.exists()) {
				if (verboseMode) {
					Debug.log("Removing %s from Database.  It's backing file is missing.", file);
				}

				if (asset.isLoaded()) {
					asset.getInstance().unload();
				}
				this.assets.remove(asset);
			}
		}

		// Compile all scripts.
		this.compiler.compileProject();
		this.extensionMapping.compileProjectMappings(this.compiler);
		this.componentList.compileProjectComponents(this.compiler);

		Collection<File> allFiles = FileUtils.listFiles(this.assetsFolder.toFile(), null, true);

		// Create new Assets.
		for (File file : allFiles) {
			AssetLocation location = new AssetLocation(file);
			if (!this.exists(location)) {
				if (verboseMode) {
					Debug.log("Adding %s to Database.", location);
				}
				this.addToDatabase(location);
			}
		}

		// Re-import Assets.
		for (CachedAsset asset : this.assets) {
			if (asset.isBuiltin()) {
				continue;
			}

			if (asset.hasFileBeenModified()) {
				this.reload(asset.location);
			}
		}

		snapshot.restore(sceneManager);

		this.invokeEvent(Phase.REBUILD);
		this.invokeEvent(Phase.POST_REBUILD);

		Debug.log("[Editor] Database rebuilt!");
	}

	/**
	 * Deletes an Asset from the Database and it's providing file from the system.
	 * If the Asset is loaded, the Asset will be unloaded first before deleting it.
	 * 
	 * @param location the location of the Asset to delete
	 * @return {@link true} if the Asset was deleted, {@link false} false if there
	 *         was an error.
	 */
	public boolean deleteAsset(AssetLocation location) {
		CachedAsset asset = this.getCachedAsset(location);
		if (asset == null) {
			Debug.logError("[Asset Database]: Could not delete Asset, no Asset exists with the path %s.", location);
			return false;
		}

		if (asset.isLoaded()) {
			asset.getInstance().unload();
		}
		this.assets.remove(asset);

		boolean error;
		try {
			error = !Desktop.getDesktop().moveToTrash(location.getFile());
		} catch (UnsupportedOperationException | SecurityException e) {
			error = true;
		}

		if (error) {
			Debug.logError("[Asset Database]: Unabled to delete Asset \"%s\"");
			return false;
		} else {
			return true; // Success!
		}
	}

	/**
	 * Creates a new Asset in the project. If an Asset already exists at
	 * {@link path} an error is logged and null is returned. If {@code path} is
	 * null, the /assets folder will be used. Asset will not be saved,
	 * {@link EditorAssetDatabase#saveAsset(SerializedJelloObject)} must be used.
	 * 
	 * @param assetClass the class to instantiate
	 * @param path       a path to the Asset relative to the \assets directory.
	 * @param assetName  the name of the asset
	 * @return the new Asset, or null on error.
	 */
	public SerializedJelloObject createAsset(Class<? extends SerializedJelloObject> assetClass, Path path,
			String assetName) {
		String fileName = assetName + "." + SerializedJelloObject.EXTENSION;
		Path pathWithNameAndExtension = path == null ? Path.of(fileName) : path.resolve(fileName);
		if (this.exists(new AssetLocation(path))) {
			Debug.logError("[Asset Database]: Could not create Asset, an Asset already exists at %s", path);
			return null;
		}

		AssetLocation location = new AssetLocation(pathWithNameAndExtension);
		SerializedJelloObject instance = (SerializedJelloObject) this.instantiateAsset(assetClass, location);
		this.saveAsset(instance);

		CachedAsset cachedAsset = new CachedAsset(location, assetClass);
		cachedAsset.setInstance(instance);
		this.assets.add(cachedAsset);

		return instance;
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	public boolean addAsset(AssetLocation location) {
		return this.addToDatabase(location) != null;
	}

	/**
	 * Saves a {@link SerializedJelloObject} Asset to disk.
	 * 
	 * @param asset the Asset to save.
	 * @return {@code true} if the Asset could be saved, {@code false} if there was
	 *         an error.
	 */
	public boolean saveAsset(SerializedJelloObject asset) {
		if (asset == null) {
			throw new IllegalArgumentException("asset may not be null");
		}

		boolean success = false;
		try {
			success = this.serializer.serializeScriptableJelloObject(asset);
		} catch (IOException e) {
			Debug.log(e);
		}
		return success;
	}

	/**
	 * Renames an Asset. If {@code assetPath} is invalid, or there was an error
	 * renaming the file, an error is logged.
	 * 
	 * @param assetPath a path to the Asset relative to the \assets directory.
	 * @param newName   the Assets new name, without an extension.
	 * @return {@link true} if the Asset was renamed, {@link false} false if there
	 *         was an error.
	 */
	public boolean renameAsset(AssetLocation location, String newName) {
		if (StringUtils.isWhitespace(newName)) {
			Debug.logError("[Asset Database]: newName may not be blank or whitespaces.");
			return false;
		}

		CachedAsset asset = this.getCachedAsset(location);
		if (asset == null) {
			Debug.logError("[Asset Database]: Unable to rename %s, it does not exist.", location);
			return false;
		}

		File newFile = JelloFileUtils.renameFile(location.getFile(), newName);
		if (newFile != null) {
			asset.location.updateLocation(this.toRelativePath(newFile.toPath()));
			return true;
		} else {
			Debug.logError("[Asset Database]: Unable to rename %s to %s", location, newName);
			return false;
		}
	}

	/**
	 * Moves an Asset to a new directory.
	 * 
	 * @param assetPath
	 * @param destination the directory to move the Asset to.
	 * @return {@link true} if the Asset was moved, {@link false} false if there was
	 *         an error.
	 */
	public boolean moveAsset(AssetLocation location, AssetLocation newLocation) {
		if (!newLocation.getFile().isDirectory()) {
			Debug.logError("[Asset Database]: destination must be a directory");
		}

		throw new NotImplementedException(); // TODO implement
	}

	/**
	 * Reloads an Asset. If the Asset is not loaded, it will be loaded and no reload
	 * will occur.
	 * 
	 * @param assetPath
	 */
	public void reload(AssetLocation location) {
		if(this.isLoaded(location)) {
			this.unload(location);
		}
		this.getAsset(location); // Reloads the Asset.
	}

	private void invokeEvent(Phase phase) {
		JelloEditor.instance.raiseEvent(ProjectReloadListener.class, (listener) -> {
			listener.onProjectReload(phase);
		});
	}
	
	/**
	 * Takes a full path and converts it to a relative path. If the path is already
	 * a relative path, nothing happens.
	 * <p>
	 * D:\MyProject\assets\water.jelobj -> water.jelobj
	 * <p>
	 * D:\MyProject\assets\materials\water.jelobj -> materials\water.jelobj
	 * </p>
	 * 
	 * @param fullPath a full path.
	 * @return the path relative to the /assets directory.
	 */
	private Path toRelativePath(Path fullPath) {
		return this.assetsFolder.relativize(fullPath);
	}
}
