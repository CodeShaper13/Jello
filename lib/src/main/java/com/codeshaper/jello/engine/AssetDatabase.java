package com.codeshaper.jello.engine;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

import com.codeshaper.jello.engine.AssetFileExtension.Internal;
import com.codeshaper.jello.engine.asset.Asset;
import com.codeshaper.jello.engine.asset.GenericAsset;

import static org.reflections.scanners.Scanners.*;

public class AssetDatabase {

	private final File projectFolder;
	private final HashMap<String, Class<Asset>> extenstionToBuiltinAssetMapping;
	private final HashMap<String, Class<Asset>> extenstionTo3rdPartyAssetMapping;

	/**
	 * A collection of all assets in the project folder. If the asset hasn't been
	 * loaded yet, the {@linkplain value} is null.
	 */
	private final HashMap<File, Asset> assets;

	public AssetDatabase(File projectFolder) {
		this.projectFolder = projectFolder;
		this.extenstionToBuiltinAssetMapping = new HashMap<String, Class<Asset>>();
		this.extenstionTo3rdPartyAssetMapping = new HashMap<String, Class<Asset>>();

		Reflections builtinScan = new Reflections("com.codeshaper.jello.engine.asset");
		Set<Class<?>> assetTypes = builtinScan
				.get(TypesAnnotated.of(AssetFileExtension.class, Internal.class).asClass());

		for (Class<?> clazz : assetTypes) {
			for (AssetFileExtension assetType : clazz.getAnnotationsByType(AssetFileExtension.class)) {
				int modifiers = clazz.getModifiers();
				if (Modifier.isAbstract(modifiers)) {
					Debug.logError("AssetFileExtention annotations are not allowed on abstract classes.");
					break;
				}
				if (Modifier.isInterface(modifiers)) {
					Debug.logError("AssetFileExtention annotations are not allowed on interfaces");
					break;
				}
				if (!Asset.class.isAssignableFrom(clazz)) {
					Debug.logError("AssetFileExtention annotations are only allowed on subclasses of Asset");
					break;
				}

				String extention = StringUtils.stripStart(assetType.value(), ".");
				this.extenstionToBuiltinAssetMapping.put(extention, (Class<Asset>) clazz);
			}
		}

		// Debug
		for (var kvp : this.extenstionToBuiltinAssetMapping.keySet()) {
			System.out.println(kvp + " -> " + this.extenstionToBuiltinAssetMapping.get(kvp));
		}

		this.assets = new HashMap<File, Asset>();
	}

	public void cacheAssets() {
		this.assets.forEach((file, asset) -> {
			if (asset != null) {
				asset.cleanup();
			}
		});

		this.assets.clear();

		Iterator<File> iter = FileUtils.iterateFiles(this.projectFolder, null, true);
		File file;
		while (iter.hasNext()) {
			file = iter.next();

			this.assets.put(file, null);
		}

	}

	/**
	 * Checks if an asset has been loaded.
	 * 
	 * @param assetFile The asset file to check for. If the file does not exist,
	 *                  {@link false} is returned.
	 * @return {@link true} if the asset has been loaded, {@link false} if it has
	 *         not been.
	 */
	public boolean isLoaded(File assetFile) {
		if (this.assets.containsKey(assetFile)) {
			return false;
		}

		return this.assets.get(assetFile) != null;
	}

	/**
	 * 
	 * @param assetFilePath
	 * @return
	 */
	public Asset getAsset(String assetFilePath) {
		return this.getAsset(new File(assetFilePath));
	}

	/**
	 * 
	 * @param assetFile
	 * @return
	 */
	public Asset getAsset(File assetFile) {
		if (this.assets.containsKey(assetFile)) {
			Asset asset = this.assets.get(assetFile);
			if (asset == null) {
				// Create an instance of the asset.
				Class<? extends Asset> clazz = this.getAssetClass(FilenameUtils.getExtension(assetFile.getName()));
				try {
					Constructor<? extends Asset> ctor = clazz.getDeclaredConstructor(File.class);
					asset = ctor.newInstance(assetFile);
					this.assets.put(assetFile, asset);
				} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException | ExceptionInInitializerError e) {
					e.printStackTrace();
					return null;
				}
			}

			return asset;
		} else {
			Debug.logError("No asset could be found at %s", assetFile.getPath());
		}

		return null;
	}

	/**
	 * Unloads teh asset by preforming any cleanup with {@link Asset#cleanup()}.
	 * This should remove any references that the asset holds to native objects. If
	 * any references exist to the java object, the asset will still be kept in
	 * memory until the references are gone like any java object.
	 * 
	 * @param assetFile
	 */
	public void unload(File assetFile) {
		if (!this.isLoaded(assetFile)) {
			return;
		}

		Asset asset = this.getAsset(assetFile);
		asset.cleanup();

		this.assets.put(assetFile, null);
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

	private Class<? extends Asset> getAssetClass(String extension) {
		Class<Asset> clazz = this.extenstionTo3rdPartyAssetMapping.get(extension);
		if (clazz != null) {
			return clazz;
		} else {
			// To user defined asset, check if there is a builtin provider class.
			clazz = this.extenstionToBuiltinAssetMapping.get(extension);
			if (clazz == null) {
				// Unknown extension.
				return GenericAsset.class;
			} else {
				return clazz;
			}
		}
	}
}
