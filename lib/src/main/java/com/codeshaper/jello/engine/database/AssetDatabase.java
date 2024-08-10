package com.codeshaper.jello.engine.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.commons.io.FilenameUtils;

import com.codeshaper.jello.engine.AssetLocation;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.asset.Asset;
import com.codeshaper.jello.engine.asset.Material;
import com.codeshaper.jello.engine.asset.SerializedJelloObject;
import com.codeshaper.jello.engine.component.JelloComponent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

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

	private final String[] builtinAsset = new String[] {
			// Audio Clips
			"builtin/beep.ogg",
			// Font
			"builtin/arial.ttf",
			// Meshes
			"builtin/meshes/cone.blend",
			"builtin/meshes/cube.blend",
			"builtin/meshes/cylinder.blend",
			"builtin/meshes/quad.blend",
			"builtin/meshes/sphere.blend",
			"builtin/meshes/torus.blend",
			// Shader Source Files
			"builtin/shaders/scene.vert",
			"builtin/shaders/scene.frag",
			// Shaders
			"builtin/shaders/error.shader",
			"builtin/shaders/unlitTexture.shader",
			"builtin/shaders/standard.shader",
			// Textures
			"builtin/textures/placeholderTexture.png", };

	private static AssetDatabase instance;

	/**
	 * The /assets folder located in the root of the project folder.
	 */
	public final Path assetsFolder;

	protected final List<CachedAsset> assets;
	protected final ExtentionMapping extentionMapping;
	protected final ComponentList componentList;

	protected AssetTypeAdapterFactory assetAdapterFactory;
	protected SerializedJelloObjectInstanceCreator serializedJelloObjectInstanceCreator;

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

		this.extentionMapping = new ExtentionMapping();
		this.componentList = new ComponentList();

		this.assetAdapterFactory = new AssetTypeAdapterFactory();
		this.serializedJelloObjectInstanceCreator = new SerializedJelloObjectInstanceCreator(null, null);

		this.buildDatabase();
	}

	/**
	 * Builds the Asset database from scratch.
	 */
	private void buildDatabase() {
		this.unloadAll();

		this.assets.clear();

		// Add the builtin Assets to the list.
		for (String stringPath : this.builtinAsset) {
			Path path = Path.of(stringPath);
			this.tryAddAsset(path);
		}

		/*
		// Add all Assets in the /assets directory to the list.
		Iterator<File> iter = FileUtils.iterateFiles(this.assetsFolder.toFile(), null, true);
		while (iter.hasNext()) {
			Path path = iter.next().toPath();
			this.tryAddAsset(this.toRelativePath(path));
		}
		*/
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
	 * Unloads the Asset by performing any cleanup with {@link Asset#cleanup()}.
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
			asset.instance.cleanup();
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
				asset.instance.cleanup();
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

	public GsonBuilder createGsonBuilder() {
		GsonBuilder builder = new GsonBuilder();

		builder.setPrettyPrinting();
		builder.serializeNulls();

		RuntimeTypeAdapterFactory<JelloComponent> componentAdapterFactory = RuntimeTypeAdapterFactory
				.of(JelloComponent.class);
		for (Class<JelloComponent> component : this.componentList) {
			componentAdapterFactory.registerSubtype(component, component.getName());
		}
		builder.registerTypeAdapterFactory(componentAdapterFactory);

		this.assetAdapterFactory.wroteRoot = true;
		builder.registerTypeAdapterFactory(this.assetAdapterFactory);

		return builder;
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
			Path fullAssetPath = toFullPath(assetPath);
			AssetLocation location = new AssetLocation(assetPath);
			Asset newInstance;
			Class<? extends Asset> providingClass = cachedAsset.getProvidingClass();
			if (SerializedJelloObject.class.isAssignableFrom(providingClass)) {
				// Special construction case.
				try (BufferedReader br = new BufferedReader(new FileReader(fullAssetPath.toFile()))) {
					br.readLine(); // Skip the first line, it states the providing class and is not valid JSON.

					GsonBuilder builder = this.createGsonBuilder();
					builder.registerTypeAdapter(providingClass,
							new SerializedJelloObjectInstanceCreator(providingClass, location));
					Gson gson = builder.create();

					newInstance = (Asset) gson.fromJson(br, providingClass);
					((SerializedJelloObject) newInstance).onDeserialize();
				} catch (IOException e) {
					e.printStackTrace();
					newInstance = null;
				}
			} else {
				newInstance = this.invokeConstructor(providingClass, location);
			}

			if (newInstance != null) {
				cachedAsset.instance = newInstance;
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

	protected class CachedAsset {

		protected final AssetLocation location;
		protected final Class<? extends Asset> providingClass;
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
		 * @return
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
			return this.location.pointsToBuiltin();
		}
	}

	private class SerializedJelloObjectInstanceCreator implements InstanceCreator<Asset> {

		private final Class<? extends Asset> clazz;
		private final AssetLocation location;

		public SerializedJelloObjectInstanceCreator(Class<? extends Asset> clazz, AssetLocation location) {
			this.clazz = clazz;
			this.location = location;
		}

		@Override
		public Asset createInstance(Type type) {
			return invokeConstructor(this.clazz, this.location);
		}
	}

	public class AssetTypeAdapterFactory implements TypeAdapterFactory {

		public boolean wroteRoot;

		public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
			if (!Asset.class.isAssignableFrom(type.getRawType())) {
				return null;
			}

			TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);

			return new TypeAdapter<T>() {

				public void write(JsonWriter out, T value) throws IOException {
					if (!wroteRoot) {
						wroteRoot = true;
						delegate.write(out, value);
					} else {
						Asset asset = (Asset) value;
						if (asset == null) {
							out.nullValue();
						} else {
							if (asset.isRuntimeAsset()) {
								out.nullValue();
							} else {
								Path path = ((Asset) value).location.getPath();
								out.value(path.toString());
							}
						}
					}
				}

				@SuppressWarnings("unchecked")
				public T read(JsonReader in) throws IOException {
					JsonToken token = in.peek();
					if (token == JsonToken.STRING) {
						String relativePath = in.nextString();
						Asset asset = getAsset(relativePath);
						return (T) asset;
					} else if (token == JsonToken.NULL) {
						in.nextNull();
						return null;
					} else {
						return delegate.read(in);
					}
				}
			};
		}
	}
}
