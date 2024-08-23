package com.codeshaper.jello.engine.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.file.attribute.FileTimes;

import com.codeshaper.jello.engine.AssetLocation;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.JelloComponent;
import com.codeshaper.jello.engine.asset.Asset;
import com.codeshaper.jello.engine.asset.GenericAsset;
import com.codeshaper.jello.engine.asset.Material;
import com.codeshaper.jello.engine.asset.SerializedJelloObject;

/**
 * An AssetDatabase is responsible for providing a way to access all assets. If
 * in the editor, this is all files within the /assets folder, and if in a
 * build, all files included in the resources folder of the build.
 * <p>
 * All {@link Path}s that are passed to methods should be relative to the
 * /assets folder. To get a texture in the "textures" folder you would use:
 * <p>
 * {@code assetDatabase.getAsset("textures/grass.png"); }
 * <p>
 * For builtin assets:
 * <p>
 * {@code assetDatabase.getAsset("builtin/textures/placeholderTexture.png"); }
 */
public class AssetDatabase {

	private static AssetDatabase instance;

	/**
	 * The /assets folder located in the root of the project folder.
	 */
	public final Path assetsFolder;

	protected final List<CachedAsset> assets;
	protected final ExtensionMapping extensionMapping;
	protected final ComponentList componentList;

	public Serializer serializer;

	public static AssetDatabase getInstance() {
		return instance;
	}

	public AssetDatabase(Path projectFolder) {
		if (instance != null) {
			Debug.logError("An AssetDatabase has already been created!");
		} else {
			instance = this;
		}

		this.assetsFolder = projectFolder;
		this.assets = new ArrayList<CachedAsset>();

		this.extensionMapping = new ExtensionMapping();
		this.componentList = new ComponentList();

		this.serializer = new Serializer(this);

		// Add the builtin Assets to the list.
		for (String stringPath : this.getBuiltinAssetPaths()) {
			AssetLocation location = new AssetLocation(stringPath);
			this.tryAddAsset(location);
		}
	}

	public Iterable<Class<JelloComponent>> getallComponents() {
		return this.componentList;
	}

	/**
	 * Checks if an Asset exists.
	 * 
	 * @param location the location of the Asset
	 * @return {@link true} if the Asset exists, {@link false} if it does not.
	 */
	public boolean exists(AssetLocation location) {
		return this.getCachedAsset(location) != null;
	}

	/**
	 * Checks if an Asset has been loaded. If the Asset has not been loaded, or it
	 * does not exist, an error is logged and {@link false} is returned.
	 * 
	 * @param location the location of the Asset
	 * @return {@link true} if the Asset has been loaded.
	 */
	public boolean isLoaded(AssetLocation location) {
		CachedAsset asset = this.getCachedAsset(location);
		if (asset != null) {
			return asset.isLoaded();
		} else {
			return false;
		}
	}

	/**
	 * Finds an Asset. If the Asset has not yet been loaded, it will be loaded. If
	 * the Asset doesn't exist, an error is logged and {@code null} is returned.
	 * 
	 * @param location the location of the Asset
	 * @return
	 */
	public Asset getAsset(AssetLocation location) {
		Asset asset = this.getAssetInternal(location);
		if (asset == null) {
			this.logMissingAssetError(location);
			return null;
		} else {
			return asset;
		}
	}

	/**
	 * Returns a list of Paths to all Assets of a specific type.
	 * 
	 * @param assetType
	 * @param includeSubClasses
	 * @param loadAssets
	 * @return
	 */
	public List<AssetLocation> getAllAssets(Class<? extends Asset> assetType, boolean includeSubClasses) {
		List<AssetLocation> locations = new ArrayList<AssetLocation>();

		for (CachedAsset asset : this.assets) {
			if (includeSubClasses) {
				if (assetType.equals(asset.getProvidingClass())) {
					locations.add(asset.location);
				}
			} else {
				if (assetType.isAssignableFrom(asset.getProvidingClass())) {
					locations.add(asset.location);
				}
			}
		}

		return locations;
	}

	/**
	 * Unloads an Asset. This will request that the Asset releases any native
	 * objects and frees up as much memory as it can by called
	 * {@link Asset#unload()}.
	 * 
	 * @param location the location of the Asset
	 * @return {@code true} if the Asset was unloaded, {@code false} if either the
	 *         Asset does not exist or the Asset was not loaded.
	 */
	public boolean unload(AssetLocation location) {
		CachedAsset asset = this.getCachedAsset(location);
		if (asset == null) {
			this.logMissingAssetError(location);
			return false;
		} else {
			if (asset.isLoaded()) {
				asset.instance.unload();
				asset.instance = null;
				return true;
			}
			return false;
		}
	}

	/**
	 * Unloads all loaded Assets by called {@link AssetDatabase#unload(File)} on
	 * every loaded asset.
	 * 
	 * @return the number of Assets that were unloaded
	 * @see AssetDatabase#unload(AssetLocation)
	 */
	public int unloadAll() {
		int count = 0;
		for (CachedAsset asset : this.assets) {
			if (this.unload(asset.location)) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Takes a relative path and converts it to a full path. If the path is already
	 * a full path, or starts with \builtin, noting happens.
	 * <p>
	 * water.jelobj -> D:\MyProject\assets\water.jelobj materials\water.jelobj ->
	 * D:\MyProject\assets\materials\water.jelobj builtin\shader.vert ->
	 * builtin\shader.vert
	 * </p>
	 * 
	 * @param path a relative path.
	 * @return a full path.
	 */
	public Path toFullPath(Path path) {
		if (!path.startsWith("builtin")) {
			// Convert the path to a full path (starts at C:\ or whatever, instead of
			// \assets).
			return this.assetsFolder.resolve(path);
		}
		return path;
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
	public Path toRelativePath(Path fullPath) {
		return this.assetsFolder.relativize(fullPath);
	}

	public TableModel getTableModel() {
		DefaultTableModel model = new DefaultTableModel(this.assets.size(), 4);
		model.setColumnIdentifiers(new String[] { "Path:", "Class:", "Is Loaded?", "" });
		for (int i = 0; i < this.assets.size(); i++) {
			CachedAsset asset = this.assets.get(i);
			model.setValueAt(asset.getLocation().getPath(), i, 0);
			model.setValueAt(asset.getProvidingClass().getSimpleName(), i, 1);
			model.setValueAt(asset.isLoaded(), i, 2);
			String s = asset.instance != null ? asset.instance.location.getPath().toString() : "NUL";
			model.setValueAt(s, i, 3);
		}

		return model;
	}

	protected CachedAsset tryAddAsset(AssetLocation location) {
		Class<? extends Asset> clazz = this.getProvidingClass(location);
		CachedAsset cachedAsset = new CachedAsset(location, clazz);
		this.assets.add(cachedAsset);
		return cachedAsset;
	}

	/**
	 * 
	 * @param location the location of the Asset
	 * @return
	 */
	protected CachedAsset getCachedAsset(AssetLocation location) {
		CachedAsset asset;
		for (int i = 0; i < this.assets.size(); i++) {
			asset = this.assets.get(i);
			if (asset.location.equals(location)) {
				return asset;
			}
		}
		return null;
	}

	/**
	 * Gets the java class that provides the implementation of the Asset based on
	 * the Asset's file type.
	 * <p>
	 * If the file's type is .jeloobj, the file will be opened to determine what
	 * subclass of {@link SerializedJelloObject} created the file. If an IO error
	 * occurs, the class can't be found, or an exception is thrown in the class's
	 * initiation, and error is logged and {@link GenericAsset} is returned.
	 * <p>
	 * If the file type has no associated java class, {@link GenericAsset} will be
	 * returned.
	 * 
	 * @param location the location of the Asset
	 * @return
	 */
	private Class<? extends Asset> getProvidingClass(AssetLocation location) {
		String extension = location.getExtension();
		Class<? extends Asset> clazz = this.extensionMapping.getAssetClass(extension);
		if (clazz == SerializedJelloObject.class) {
			String jelloObjectClassName = null;
			try (BufferedReader br = new BufferedReader(new FileReader(location.getFullPath().toFile()))) {
				jelloObjectClassName = br.readLine();
				@SuppressWarnings("unchecked")
				Class<? extends Asset> jelloObjCls = (Class<? extends Asset>) Class.forName(jelloObjectClassName);
				return jelloObjCls;
			} catch (IOException e) {
				Debug.logError("[Asset Database]: Unable to get the class for \"%s\", there was an IO error.",
						location);
			} catch (ExceptionInInitializerError e) {
				Debug.log(e); // TODO
			} catch (ClassNotFoundException e) {
				Debug.logError(
						"[Asset Database]: Unable to find a class with the name \"%s\"",
						jelloObjectClassName);
			}
			return GenericAsset.class;
		} else {
			return clazz;
		}
	}

	/**
	 * 
	 * @param location
	 * @return
	 * @throws IllegalArgumentException if location is null.
	 */
	private Asset getAssetInternal(AssetLocation location) {
		if (location == null) {
			throw new IllegalArgumentException("location may not be null");
		}
		
		CachedAsset cachedAsset = this.getCachedAsset(location);
		if (cachedAsset.isLoaded()) {
			return cachedAsset.instance;
		} else {
			// Asset has not been loaded, instantiate the asset.
			Asset newInstance;
			Class<? extends Asset> providingClass = cachedAsset.getProvidingClass();
			if (SerializedJelloObject.class.isAssignableFrom(providingClass)) {
				@SuppressWarnings("unchecked")
				Class<SerializedJelloObject> cls = (Class<SerializedJelloObject>) providingClass;
				try {
					newInstance = this.serializer.deserialize(location, cls);
				} catch (IOException e) {
					Debug.log(e);
					newInstance = null;
				}
			} else {
				newInstance = this.invokeConstructor(providingClass, location);
				if (newInstance != null) {
					newInstance.load();
				}
			}

			if (newInstance != null) {
				cachedAsset.instance = newInstance;
				cachedAsset.lastLoaded = FileTimes.now();
				return newInstance;
			} else {
				Debug.logWarning("[AssetDatabase]: Error constructing Asset."); // TODO explain the error.
				return null;
			}
		}
	}

	protected Asset invokeConstructor(Class<? extends Asset> clazz, AssetLocation location) {
		try {
			Constructor<? extends Asset> ctor = clazz.getDeclaredConstructor(AssetLocation.class);
			return (Asset) ctor.newInstance(location);
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException exception) {
			this.logError(exception.getMessage(), exception);
		} catch (ExceptionInInitializerError exception) {
			this.logError("A static initializer threw an exception.", exception);
		} catch (InvocationTargetException exception) {
			this.logError("The constructor threw an exception", exception);
		} catch (InstantiationException exception) {
			this.logError("Asset is an abstract class", exception);
		} catch (NoSuchMethodException exception) {
			this.logError(
					"Asset does not have a public constructor taking a single argument of type java.nio.file.Path",
					exception);
		}

		return null;
	}

	private void logError(String msg, Throwable exception) {
		Debug.logError("[Asset Database]: Error creating Asset: " + msg);
		if (exception != null) {
			exception.printStackTrace();
		}
	}

	/**
	 * Gets a list of the paths to the builtin assets. On error, an empty list is
	 * returned.
	 * 
	 * @return a list of paths to the builtin assets.
	 */
	private List<String> getBuiltinAssetPaths() {
		try (InputStream stream = AssetDatabase.class.getResourceAsStream("/builtinAssets.txt")) {
			List<String> lines = IOUtils.readLines(stream, StandardCharsets.UTF_8);
			for (int i = lines.size() - 1; i >= 0; --i) {
				String line = lines.get(i);
				if (line.isBlank() || line.trim().startsWith("#")) {
					lines.remove(i);
				}
			}
			return lines;
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<String>();
		}
	}

	private void logMissingAssetError(AssetLocation location) {
		Debug.logError(
				"[Asset Database]: No Asset exists at \"%s\"",
				location.getPath().toString());
	}

	protected class CachedAsset {

		private final AssetLocation location;
		private final Class<? extends Asset> providingClass;
		public long lastLoaded;
		/**
		 * The instance of the Asset. If the Asset is not loaded, this is null.
		 */
		public Asset instance;

		public CachedAsset(AssetLocation location, Class<? extends Asset> providingClass) {
			this.location = location;
			this.providingClass = providingClass;
		}
		
		public Asset getInstance() {
			return this.instance;
		}
		
		public void setInstance(Asset instance) {
			this.instance = instance;
			this.lastLoaded = Date.
		}

		/**
		 * Gets relative path to the file providing the Asset, starting at the /assets
		 * folder, or /builtin if it's a builtin Asset.
		 * 
		 * @return a relative path to the Asset.
		 */
		public Path getPath() {
			return this.location.getPath();
		}

		// This is only used when renaming an asset.
		public void setPath(Path path) {
			this.location.updateLocation(path);
		}

		/**
		 * Gets the class that provides the implementation of the Asset in code. For
		 * Assets that extends @link {@link SerializedJelloObject} (e.g. Material), the
		 * exact class is returned (e.g. {@link Material}.
		 * 
		 * @return the class providing the implementation of the Asset.
		 */
		public Class<? extends Asset> getProvidingClass() {
			return this.providingClass;
		}

		/**
		 * Checks if the Asset is loaded.
		 * 
		 * @return {@code true} if the Asset is loaded.
		 */
		public boolean isLoaded() {
			return this.instance != null;
		}

		/**
		 * Checks if this is a builtin Asset.
		 * 
		 * @return {@link true} if this is a builtin Asset.
		 */
		public boolean isBuiltin() {
			return this.location.isBuiltin();
		}

		/**
		 * Checks if the file providing this Asset has been modified since it was last
		 * imported.
		 * 
		 * @return
		 */
		public boolean hasFileBeenModified() {
			if (this.lastLoaded == 0) {
				return false;
			} else {
				return this.lastLoaded < this.getLastModified();
			}
		}

		private long getLastModified() {
			return this.location.getFile().lastModified();
		}

		public AssetLocation getLocation() {
			return this.location;
		}
	}
}
