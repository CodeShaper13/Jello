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
import java.util.List;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.file.attribute.FileTimes;

import com.codeshaper.jello.engine.Application;
import com.codeshaper.jello.engine.AssetLocation;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.JelloComponent;
import com.codeshaper.jello.engine.asset.Asset;
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
	protected final ExtensionMapping extentionMapping;
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

		this.extentionMapping = new ExtensionMapping();
		this.componentList = new ComponentList();

		this.serializer = new Serializer(this);

		this.buildDatabase();
	}

	/**
	 * Builds the Asset database from scratch.
	 */
	private void buildDatabase() {
		this.unloadAll();

		this.assets.clear();

		// Add the builtin Assets to the list.
		for (String stringPath : this.getBuiltinAssetPaths()) {
			Path path = Path.of(stringPath);
			this.tryAddAsset(path);
		}
	}

	public Iterable<Class<JelloComponent>> getallComponents() {
		return this.componentList;
	}

	/**
	 * Checks if an Asset exists.
	 * 
	 * @param assetPath the relative path to the Asset.
	 * @return {@link true} if the Asset exists, {@link false} if it does not.
	 */
	public boolean exists(Path assetPath) {
		CachedAsset asset = this.getCachedAsset(assetPath);
		return asset != null;
	}

	/**
	 * Checks if an Asset has been loaded. If the Asset does not exist,
	 * {@link false} is returned.
	 * 
	 * @param assetFile the relative path to the Asset.
	 * @return {@link true} if the Asset has been loaded, {@link false} if it has
	 *         not been.
	 */
	public boolean isLoaded(Path assetPath) {
		CachedAsset asset = this.getCachedAsset(assetPath);
		if (asset != null) {
			return asset.isLoaded();
		} else {
			return false;
		}
	}

	/**
	 * Finds an Asset at a location. If the Asset doesn't exist, null is returned.
	 * 
	 * @param assetPath the relative path to the Asset.
	 * @return
	 */
	public Asset getAsset(String assetPath) {
		return this.getAssetInternal(Paths.get(assetPath));
	}

	/**
	 * Finds an Asset at a location. If the Asset doesn't exist, null is returned.
	 * 
	 * @param assetPath the relative path to the Asset.
	 * @return
	 */
	public Asset getAsset(Path assetPath) {
		return this.getAssetInternal(assetPath);
	}

	/**
	 * Returns a list of Paths to all Assets of a specific type.
	 * 
	 * @param assetType
	 * @param includeSubClasses
	 * @return
	 */
	public List<Path> getAllAssetsOfType(Class<? extends Asset> assetType, boolean includeSubClasses) {
		List<Path> paths = new ArrayList<Path>();

		for (CachedAsset asset : this.assets) {
			if (includeSubClasses) {
				if (assetType.equals(asset.getProvidingClass())) {
					paths.add(asset.getPath());
				}
			} else {
				if (assetType.isAssignableFrom(asset.getProvidingClass())) {
					paths.add(asset.getPath());
				}
			}
		}

		return paths;
	}

	/**
	 * Unloads the Asset by performing any cleanup with {@link Asset#unload()}.
	 * This should remove any references that the Asset holds to native objects. If
	 * any references exist to the java object, the asset will still be kept in
	 * memory until the references are gone like any java object.
	 * 
	 * @param assetFile
	 */
	public void unload(Path assetFile) {
		this.unload(this.getCachedAsset(assetFile));
	}

	protected void unload(CachedAsset asset) {
		if (asset.isLoaded()) {
			asset.instance.unload();
			asset.instance = null;
		}
	}

	/**
	 * Unloads all loaded assets by called {@link AssetDatabase#unload(File)} on
	 * every loaded asset.
	 */
	public void unloadAll() {
		for (CachedAsset asset : this.assets) {
			if (asset.isLoaded()) {
				asset.instance.unload();
				asset.instance = null;
			}
		}
	}
	
	public TableModel getTableModel() {
		DefaultTableModel model = new DefaultTableModel(this.assets.size(), 4);
		model.setColumnIdentifiers(new String[] { "Path:", "Class:", "Is Loaded?", "" });
		for (int i = 0; i < this.assets.size(); i++) {
			CachedAsset asset = this.assets.get(i);
			model.setValueAt(asset.getPath(), i, 0);
			model.setValueAt(asset.getProvidingClass().getSimpleName(), i, 1);
			model.setValueAt(asset.isLoaded(), i, 2);
			String s = asset.instance != null ? asset.instance.location.getPath().toString() : "NUL";
			model.setValueAt(s, i, 3);
		}

		return model;
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

	protected CachedAsset tryAddAsset(Path relativePath) {
		Class<? extends Asset> clazz = this.getProvidingClass(relativePath);
		if (clazz == null) {
			Debug.log("ERROR!"); // TODO
			return null;
		} else {
			AssetLocation location = new AssetLocation(relativePath);
			CachedAsset cachedAsset = new CachedAsset(location, clazz);
			this.assets.add(cachedAsset);
			return cachedAsset;
		}
	}

	/**
	 * 
	 * @param assetPath a path relative to the /assets folder.
	 * @return
	 */
	protected CachedAsset getCachedAsset(Path assetPath) {
		CachedAsset asset;
		for (int i = 0; i < this.assets.size(); i++) {
			asset = this.assets.get(i);
			if (asset.getPath().equals(assetPath)) {
				return asset;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param path the path to the Asset.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Class<? extends Asset> getProvidingClass(Path path) {
		String extensions = FilenameUtils.getExtension(path.toString());
		Class<? extends Asset> clazz = this.extentionMapping.getAssetClass(extensions);
		if (clazz == SerializedJelloObject.class) {
			try (BufferedReader br = new BufferedReader(new FileReader(this.toFullPath(path).toFile()))) {
				String jelloObjectClassName = br.readLine();
				return (Class<? extends Asset>) Class.forName(jelloObjectClassName);
			} catch (IOException | ExceptionInInitializerError e) {
				e.printStackTrace();
				return null;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return clazz;
		}
	}

	private Asset getAssetInternal(Path assetPath) {
		CachedAsset cachedAsset = this.getCachedAsset(assetPath);
		if (cachedAsset == null) {
			Debug.logError("[AssetDatabase]: No asset could be found at %s", assetPath.toString());
			return null;
		}

		if (cachedAsset.isLoaded()) {
			return cachedAsset.instance;
		} else {
			// Asset has not been loaded, instantiate the asset.
			AssetLocation location = new AssetLocation(assetPath);
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
				if(newInstance != null) {
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

	protected class CachedAsset {

		private final AssetLocation location;
		private final Class<? extends Asset> providingClass;
		public FileTime lastLoaded;
		/**
		 * The instance of the Asset. If the Asset is not loaded, this is null.
		 */
		public Asset instance;

		public CachedAsset(AssetLocation location, Class<? extends Asset> providingClass) {
			this.location = location;
			this.providingClass = providingClass;
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

		/**
		 * Gets the full path to the Asset.
		 * 
		 * @return
		 */
		public Path getFullPath() {
			return this.location.getFullPath();
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
			if (this.lastLoaded == null) {
				return false;
			} else {
				FileTime t = this.getLastModified();
				if (t == null) {
					return false;
				}
				return this.lastLoaded.compareTo(t) < 0;
			}
		}

		private FileTime getLastModified() {
			try {
				return Files.getLastModifiedTime(this.location.getFullPath());
			} catch (IOException e) {
				return null;
			}
		}
	}
}
