package com.codeshaper.jello.editor;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import com.codeshaper.jello.editor.event.ProjectReloadListener;
import com.codeshaper.jello.editor.event.ProjectReloadListener.Phase;
import com.codeshaper.jello.editor.utils.JelloFileUtils;
import com.codeshaper.jello.engine.AssetLocation;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.asset.Asset;
import com.codeshaper.jello.engine.asset.Script;
import com.codeshaper.jello.engine.asset.SerializedJelloObject;
import com.codeshaper.jello.engine.database.AssetDatabase;
import com.codeshaper.jello.engine.database.ComponentList;
import com.google.gson.Gson;

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
				
		// Check all the Assets and if the file that provides them no longer exists,
		// remove the Asset from the Database.
		for (int i = this.assets.size() - 1; i >= 0; --i) {
			CachedAsset asset = this.assets.get(i);
			if (asset.isBuiltin()) {
				continue; // builtin assets are never removed.
			}
			Path path = asset.getFullPath();
			if (!Files.exists(path)) {
				if(verboseMode) {
					Debug.log("Removing %s from Database.  It's backing file is missing.", asset.getPath());
				}
				this.remove(asset);
			}
		}

		this.compiler.compileProject();
		this.extentionMapping.compileProjectMappings(this.compiler);
		this.componentList.compileProjectComponents(this.compiler);
		
		Collection<File> allFiles = FileUtils.listFiles(this.assetsFolder.toFile(), null, true);
		
		// Create new Assets.
		for(File file : allFiles) {
			Path path = this.assetsFolder.relativize(file.toPath());
			if(!this.exists(path)) {
				if(verboseMode) {
					Debug.log("Adding %s to Database.", path);
				}
				this.tryAddAsset(path);
			}
		}
		
		// Re-import Assets.
		for(File file : allFiles) {
			// TODO
		}
		
		this.invokeEvent(Phase.REBUILD);
		this.invokeEvent(Phase.POST_REBUILD);
		
		Debug.log("[Editor] Database rebuilt!");
	}

	/**
	 * Deletes an Asset from the database and it's providing file from the system.
	 * 
	 * @param assetPath a path to the Asset relative to the \assets directory.
	 * @return {@link true} if the Asset was deleted, {@link false} false if there
	 *         was an error.
	 */
	public boolean deleteAsset(Path assetPath) {
		CachedAsset asset = this.getCachedAsset(assetPath);
		if (asset == null) {
			Debug.logError("[Asset Database]: Could not delete Asset, no Asset exists with the path %s.", assetPath);
			return false;
		}

		this.remove(asset);

		boolean error;
		try {
			error = !Desktop.getDesktop().moveToTrash(this.toFullPath(assetPath).toFile());
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
		if (this.exists(path)) {
			Debug.logError("[Asset Database]: Could not create Asset, an Asset already exists at %s", path);
			return null;
		}

		AssetLocation location = new AssetLocation(pathWithNameAndExtension);
		SerializedJelloObject asset = (SerializedJelloObject) this.invokeConstructor(assetClass, location);
		this.saveAsset(asset);

		CachedAsset cachedAsset = new CachedAsset(location, assetClass);
		cachedAsset.instance = asset;
		this.assets.add(cachedAsset);

		return asset;
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	public boolean addAsset(Path path) {
		return this.tryAddAsset(path) != null;
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

		File file = asset.location.getFullPath().toFile();
		try (FileWriter writer = new FileWriter(file)) {
			// Write the class name as the first line.
			String fullClassName = asset.getClass().getName();
			writer.write(fullClassName + "\n");

			Gson gson = this.createGsonBuilder().create();

			this.assetAdapterFactory.wroteRoot = false;
			asset.onSerialize();

			gson.toJson(asset, writer);

			return true;
		} catch (IOException e) {
			e.printStackTrace();

			return false;
		}
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
	public boolean renameAsset(Path assetPath, String newName) {
		if (StringUtils.isWhitespace(newName)) {
			Debug.logError("[Asset Database]: newName may not be blank or whitespaces.");
			return false;
		}

		CachedAsset asset = this.getCachedAsset(assetPath);
		if (asset == null) {
			Debug.logError("[Asset Database]: Unable to rename %s, it does not exist.", assetPath.toString());
			return false;
		}

		File newFile = JelloFileUtils.renameFile(this.toFullPath(assetPath).toFile(), newName);
		if (newFile != null) {
			asset.setPath(this.toRelativePath(newFile.toPath()));
			return true;
		} else {
			Debug.logError("[Asset Database]: Unable to rename %s to %s", assetPath.toString(), newName);
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
	public boolean moveAsset(Path assetPath, Path destination) {
		if (!Files.isDirectory(destination)) {
			Debug.logError("[Asset Database]: destination must be a directory");
		}

		throw new NotImplementedException(); // TODO implement
	}

	protected void remove(CachedAsset asset) {
		if (asset.isLoaded()) {
			this.unload(asset);
		}
		this.assets.remove(asset);
	}
	
	private void invokeEvent(Phase phase) {
		JelloEditor.instance.raiseEvent(ProjectReloadListener.class, (listener) -> {
			listener.onProjectReload(phase);
		});
	}
}
