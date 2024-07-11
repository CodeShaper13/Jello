package com.codeshaper.jello.engine.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

import com.codeshaper.jello.editor.property.modifier.CreateAssetEntry;
import com.codeshaper.jello.engine.AssetFileExtension;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.AssetFileExtension.Internal;
import com.codeshaper.jello.engine.asset.Asset;
import com.codeshaper.jello.engine.asset.GenericAsset;
import com.codeshaper.jello.engine.asset.SerializedJelloObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;

import static org.reflections.scanners.Scanners.*;

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
			// Meshes:

			// Shaders:
			"builtin/shaders/scene.vert", "builtin/shaders/scene.frag",
			// Textures:
			"builtin/textures/placeholderTexture.png", };

	/**
	 * The /assets folder located in the root of the project folder.
	 */
	protected final Path assetsFolder;
	/**
	 * A collection of all assets in the project folder. If the asset hasn't been
	 * loaded yet, the {@linkplain value} is null.
	 * 
	 * The key is a path to the asset, relative to the /assets folder.
	 */
	protected final HashMap<Path, Asset> assets;
	protected final ExtentionMapping extentionMapping;

	public List<CreateAssetEntry.Data> createAssetEntries;

	public AssetDatabase(Path projectFolder) {
		this.assetsFolder = projectFolder;
		this.assets = new HashMap<Path, Asset>();

		Reflections scan = new Reflections("com.codeshaper.jello.engine.asset");

		this.extentionMapping = new ExtentionMapping();
		this.extentionMapping.compileBuiltinMappings(scan);

		this.createAssetEntries = new ArrayList<>();
		Set<Class<?>> createAssetEntries = scan.get(TypesAnnotated.of(CreateAssetEntry.class).asClass());
		for (Class<?> clazz : createAssetEntries) {
			CreateAssetEntry a = clazz.getAnnotation(CreateAssetEntry.class);
			int modifiers = clazz.getModifiers();
			if (Modifier.isAbstract(modifiers)) {
				Debug.logError("CreateAssetEntry annotations are not allowed on abstract classes.");
				break;
			}
			if (Modifier.isInterface(modifiers)) {
				Debug.logError("CreateAssetEntry annotations are not allowed on interfaces");
				break;
			}
			if (!SerializedJelloObject.class.isAssignableFrom(clazz)) {
				Debug.logError("CreateAssetEntry annotations are only allowed on subclasses of SerializedJelloObject");
				break;
			}

			CreateAssetEntry.Data data = new CreateAssetEntry.Data(a.fileName(), a.location(),
					(Class<? extends SerializedJelloObject>) clazz);
			this.createAssetEntries.add(data);
		}
	}

	/*
	 * private List<Path> getBuiltinAssetPaths() { Reflections reflections = new
	 * Reflections( new
	 * ConfigurationBuilder().forPackage("com.codeshaper.jello").setScanners(
	 * Resources)); Set<String> stringPaths =
	 * reflections.get(Resources.with(".*").filter(s -> s.startsWith("builtin")));
	 * 
	 * List<Path> array = new ArrayList<Path>(); for (String s : stringPaths) {
	 * //array.add(Paths.get(s)); URL url = getClass().getResource("/" + s); try {
	 * array.add(Paths.get(url.toURI())); } catch (URISyntaxException e) {
	 * e.printStackTrace(); }
	 * 
	 * }
	 * 
	 * return array; }
	 */

	/**
	 * Builds the Asset database from scratch.
	 */
	public void buildDatabase() {
		// Perform cleanup on all loaded assets.
		this.assets.forEach((file, asset) -> {
			if (asset != null) {
				asset.cleanup();
			}
		});

		this.assets.clear();

		// Add the builtin Assets to the list.
		for (String s : this.builtinAsset) {
			this.assets.put(Path.of(s), null);
		}

		// Add all Assets in the project folder to the list.
		Iterator<File> iter = FileUtils.iterateFiles(this.assetsFolder.toFile(), null, true);
		File file;
		while (iter.hasNext()) {
			file = iter.next();
			this.assets.put(file.toPath(), null);
		}
	}

	/**
	 * Checks if an Asset exists.
	 * 
	 * @param assetPath the relative path to the Asset.
	 * @return {@link true} if the Asset exists, {@link false} if it does not.
	 */
	public boolean exists(Path assetPath) {
		return this.assets.containsKey(this.toFullPath(assetPath));
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
		if (!this.exists(assetPath)) {
			return false;
		}

		return this.assets.get(this.toFullPath(assetPath)) != null;
	}

	/**
	 * Attempts to find an Asset at a location. If the Asset doesn't exist, null is
	 * returned.
	 * 
	 * @param assetPath the relative path to the Asset.
	 * @return
	 */
	public Asset getAsset(String assetPath) {
		return this.getAssetInternal(Paths.get(assetPath));
	}

	/**
	 * Attempts to find an Asset at a location. If the Asset doesn't exist, null is
	 * returned.
	 * 
	 * @param assetPath the relative path to the Asset.
	 * @return
	 */
	public Asset getAsset(Path assetPath) {
		return this.getAssetInternal(assetPath);
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
		if (!this.isLoaded(assetFile)) {
			return;
		}

		Asset asset = this.getAsset(assetFile);
		asset.cleanup();

		this.assets.put(toFullPath(assetFile), null);
	}

	/**
	 * Unloads all loaded assets by called {@link AssetDatabase#unload(File)} on
	 * every loaded asset.
	 */
	public void unloadAll() {
		for (var entry : this.assets.entrySet()) {
			if (entry.getValue() == null) {
				continue; // Asset is not loaded.
			}

			this.unload(entry.getKey());
		}
	}

	/**
	 * Takes a relative asset path and converts it to a full asset path.
	 * 
	 * @param path the relative path to the asset.
	 * @return a full path.
	 */
	protected Path toFullPath(Path path) {
		if (!path.startsWith("builtin")) {
			// Convert the path to a full path (starts at C:/ or whatever, instead of
			// /assets).
			return this.assetsFolder.resolve(path);
		}
		return path;
	}
	
	private Asset getAssetInternal(Path assetPath) {
		assetPath = toFullPath(assetPath);

		if (this.exists(assetPath)) {
			Asset asset = this.assets.get(assetPath);
			if (asset == null) {
				// Asset has not been loaded, instantiate the asset.
				Class<? extends Asset> assetsProvidingClass = this.extentionMapping
						.getAssetClass(FilenameUtils.getExtension(assetPath.getFileName().toString()));

				if (assetsProvidingClass == SerializedJelloObject.class) {
					// Special construction case.
					try (BufferedReader br = new BufferedReader(new FileReader(assetPath.toFile()))) {
						String jelloObjectClassName = br.readLine();
						Class<? extends Asset> c = (Class<? extends Asset>) Class.forName(jelloObjectClassName);

						GsonBuilder builder = new GsonBuilder();
						builder.registerTypeAdapter(c, new SerializedJelloObjectInstanceCreator(c, assetPath));

						Gson gson = builder.create();
						asset = (Asset) gson.fromJson(br, c);
					} catch (IOException | ClassNotFoundException e) {
						e.printStackTrace();
					}
				} else {
					asset = this.invokeConstructor(assetsProvidingClass, assetPath);
				}

				if (asset != null) {
					this.assets.put(assetPath, asset);
				} else {
					Debug.logWarning("[AssetDatabase]: Error constructing Asset."); // TODO explain the error.
				}
			}

			return asset;
		} else {
			Debug.logError("[AssetDatabase]: No asset could be found at %s", assetPath.toString());
			return null;
		}
	}


	private Asset invokeConstructor(Class<? extends Asset> clazz, Path assetFile) {
		try {
			Constructor<? extends Asset> ctor = clazz.getDeclaredConstructor(Path.class);
			return (Asset) ctor.newInstance(assetFile);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException | ExceptionInInitializerError e) {
			e.printStackTrace();
			return null;
		}
	}

	private class SerializedJelloObjectInstanceCreator implements InstanceCreator<Asset> {

		private final Class<? extends Asset> clazz;
		private final Path assetPath;

		public SerializedJelloObjectInstanceCreator(Class<? extends Asset> clazz, Path assetPath) {
			this.clazz = clazz;
			this.assetPath = assetPath;
		}

		@Override
		public Asset createInstance(Type type) {
			return invokeConstructor(this.clazz, this.assetPath);
		}
	}
}
