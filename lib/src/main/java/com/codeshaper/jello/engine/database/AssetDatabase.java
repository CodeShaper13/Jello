package com.codeshaper.jello.engine.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.codeshaper.jello.engine.AssetLocation;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.JelloComponent;
import com.codeshaper.jello.engine.asset.Asset;
import com.codeshaper.jello.engine.asset.GenericAsset;
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

	public void func() {
		// Add all Assets in the /assets directory to the list.
		Iterator<File> iter = FileUtils.iterateFiles(this.assetsFolder.toFile(), null, true);
		while (iter.hasNext()) {
			File file = iter.next();
			this.addToDatabase(new AssetLocation(file));
		}
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
		try {
			List<String> paths =  this.getBuiltinAssetPaths("/builtinAssets.txt");
			for (String stringPath : paths) {
				AssetLocation location = new AssetLocation(stringPath);
				this.addToDatabase(location);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Iterable<Class<JelloComponent>> getallComponents() {
		return this.componentList;
	}

	/**
	 * Checks if an Asset exists.
	 * 
	 * @param location the location of the Asset
	 * @return {@link true} if the Asset exists, {@link false} if it does not
	 * @throws IllegalArgumentException if location is {@code null}
	 */
	public boolean exists(AssetLocation location) {
		if (location == null) {
			throw new IllegalArgumentException("location may not be null");
		}

		return this.getCachedAsset(location) != null;
	}

	/**
	 * Checks if an Asset has been loaded. If the Asset has not been loaded, or it
	 * does not exist, an error is logged and {@link false} is returned.
	 * 
	 * @param location the location of the Asset
	 * @return {@link true} if the Asset has been loaded.
	 * @throws IllegalArgumentException if location is {@code null}
	 */
	public boolean isLoaded(AssetLocation location) {
		if (location == null) {
			throw new IllegalArgumentException("location may not be null");
		}

		CachedAsset asset = this.getCachedAsset(location);
		if (asset != null) {
			return asset.isLoaded();
		} else {
			this.logMissingAssetError(location);
			return false;
		}
	}

	/**
	 * Finds an Asset. If the Asset has not yet been loaded, it will be loaded. If
	 * the Asset doesn't exist, an error is logged and {@code null} is returned.
	 * 
	 * @param location the location of the Asset
	 * @return
	 * @throws IllegalArgumentException if location is {@code null}
	 */
	public Asset getAsset(AssetLocation location) {
		if (location == null) {
			throw new IllegalArgumentException("location may not be null");
		}

		Asset asset = this.getAssetInternal(location);
		if (asset != null) {
			return asset;
		} else {
			this.logMissingAssetError(location);
			return null;
		}
	}

	/**
	 * Gets a list of {@link AssetLocation}s pointing to all Assets of a specific
	 * type within the project.
	 * 
	 * @param assetType
	 * @param includeSubClasses should sub classes be include or only the exact type
	 * @return An {@link ArrayList} List of Assets with a certain type. If no Assets
	 *         exists with that type, an empty collection is returned.
	 * @throws IllegalArgumentException if assetType is null
	 */
	public List<AssetLocation> getAllAssets(Class<? extends Asset> assetType, boolean includeSubClasses) {
		if (assetType == null) {
			throw new IllegalArgumentException("assetType may not be null");
		}

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
	 * objects and frees up as much memory as it can by calling
	 * {@link Asset#unload()}.
	 * 
	 * @param location the location of the Asset
	 * @return {@code true} if the Asset was unloaded, {@code false} if either the
	 *         Asset does not exist or the Asset was not currently loaded.
	 * @throws IllegalArgumentException if location is {@code null}
	 */
	public boolean unload(AssetLocation location) {
		if (location == null) {
			throw new IllegalArgumentException("location may not be null");
		}

		CachedAsset asset = this.getCachedAsset(location);
		if (asset == null) {
			this.logMissingAssetError(location);
			return false;
		} else {
			if (asset.isLoaded()) {
				asset.getInstance().unload();
				asset.setInstance(null);
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

	public TableModel getTableModel() {
		DefaultTableModel model = new DefaultTableModel(this.assets.size(), 4);
		model.setColumnIdentifiers(new String[] { "Path:", "Class:", "Is Loaded?", "" });
		for (int i = 0; i < this.assets.size(); i++) {
			CachedAsset cachedAsset = this.assets.get(i);
			model.setValueAt(cachedAsset.location.getRelativePath(), i, 0);
			model.setValueAt(cachedAsset.getProvidingClass().getSimpleName(), i, 1);
			model.setValueAt(cachedAsset.isLoaded(), i, 2);
			Asset asset = cachedAsset.getInstance();
			String s = asset != null ? asset.location.getRelativePath().toString() : "NUL";
			model.setValueAt(s, i, 3);
		}

		return model;
	}

	/**
	 * 
	 * @param location
	 * @return
	 * @throws IllegalArgumentException if location is null
	 */
	protected CachedAsset addToDatabase(AssetLocation location) {
		if (location == null) {
			throw new IllegalArgumentException("location may not be null");
		}

		if (this.exists(location)) {
			return null;
		}

		CachedAsset cachedAsset = new CachedAsset(location, this.getProvidingClass(location));
		this.assets.add(cachedAsset);
		return cachedAsset;
	}

	/**
	 * Gets the {@link CachedAsset} for an Asset at location.
	 * 
	 * @param location the location of the Asset
	 * @return
	 * @throws IllegalArgumentException if location is null
	 */
	protected CachedAsset getCachedAsset(AssetLocation location) {
		if (location == null) {
			throw new IllegalArgumentException("location may not be null");
		}

		CachedAsset asset;
		for (int i = 0; i < this.assets.size(); i++) {
			asset = this.assets.get(i);
			if (asset.location.equals(location)) {
				return asset;
			}
		}
		return null;
	}

	protected Asset instantiateAsset(Class<? extends Asset> clazz, AssetLocation location) {
		try {
			Constructor<? extends Asset> constructor = clazz.getDeclaredConstructor(AssetLocation.class);
			return (Asset) constructor.newInstance(location);
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
			try (BufferedReader br = new BufferedReader(new FileReader(location.getFile()))) {
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
		if(cachedAsset == null) {
			this.logMissingAssetError(location);
			return null;
		}
		
		if (cachedAsset.isLoaded()) {
			return cachedAsset.getInstance();
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
				newInstance = this.instantiateAsset(providingClass, location);
				if (newInstance != null) {
					newInstance.load();
				}
			}

			if (newInstance != null) {
				cachedAsset.setInstance(newInstance);
				return newInstance;
			} else {
				Debug.logWarning("[AssetDatabase]: Error constructing Asset."); // TODO explain the error.
				return null;
			}
		}
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
	 * @throws IOException if there is an IO error
	 */
	private List<String> getBuiltinAssetPaths(String resource) throws IOException {
		try (InputStream stream = AssetDatabase.class.getResourceAsStream(resource)) {
			List<String> lines = IOUtils.readLines(stream, StandardCharsets.UTF_8);
			for (int i = lines.size() - 1; i >= 0; --i) {
				String line = lines.get(i);
				if (line.isBlank() || line.trim().startsWith("#")) {
					lines.remove(i);
				}
			}
			return lines;
		}
	}
	

	private void logMissingAssetError(AssetLocation location) {
		Debug.logError(
				"[Asset Database]: No Asset exists at \"%s\"",
				location.getRelativePath().toString());
	}
}
