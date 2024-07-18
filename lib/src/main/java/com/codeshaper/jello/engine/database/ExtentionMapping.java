package com.codeshaper.jello.engine.database;

import static org.reflections.scanners.Scanners.TypesAnnotated;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

import com.codeshaper.jello.engine.AssetFileExtension;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.asset.Asset;
import com.codeshaper.jello.engine.asset.GenericAsset;

/**
 * Provides a mapping between file extensions and the {@link Assets} that
 * provide them.
 */
class ExtentionMapping {

	// Separate lists, only the later has to rebuilt while the application is
	// running.
	private final HashMap<String, Class<Asset>> extenstionToBuiltinAsset;
	private final HashMap<String, Class<Asset>> extenstionTo3rdPartyAsset;

	public ExtentionMapping() {
		this.extenstionToBuiltinAsset = new HashMap<String, Class<Asset>>();
		this.extenstionTo3rdPartyAsset = new HashMap<String, Class<Asset>>();
	}

	/**
	 * @param scan A scan of com.codeshaper.jello.engine.assets package.
	 */
	public void compileBuiltinMappings(Reflections scan) {
		Set<Class<?>> assetTypes = scan.get(TypesAnnotated.of(AssetFileExtension.class, AssetFileExtension.Internal.class).asClass());
		this.populateMapping(assetTypes, this.extenstionToBuiltinAsset);
	}

	public void compileThirdPartyMappings() {
		// TODO not yet implemented or called.
	}

	/**
	 * Gets the Asset class that provides the implementation of a file. If there is
	 * no Asset for the passed extension, a {@link GenericAsset} will be returned.
	 * 
	 * @param extension the files extensions, without the ".".
	 * @return An Asset class providing the implementation of a file.
	 */
	public Class<? extends Asset> getAssetClass(String extension) {
		Class<Asset> clazz = this.extenstionTo3rdPartyAsset.get(extension);
		if (clazz != null) {
			return clazz;
		} else {
			// To user defined asset, check if there is a builtin provider class.
			clazz = this.extenstionToBuiltinAsset.get(extension);
			if (clazz == null) {
				// Unknown extension.
				return GenericAsset.class;
			} else {
				return clazz;
			}
		}
	}
	
	private void populateMapping(Set<Class<?>> classes, HashMap<String, Class<Asset>> mapping) {
		for (Class<?> clazz : classes) {
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
				if (!Modifier.isPublic(modifiers)) {
					Debug.logError("AssetFileExtention annotations are not allowed on public classse");
					break;
				}
				if (!Asset.class.isAssignableFrom(clazz)) {
					Debug.logError("AssetFileExtention annotations are only allowed on subclasses of Asset");
					break;
				}

				String extention = StringUtils.stripStart(assetType.value(), ".");
				mapping.put(extention, (Class<Asset>) clazz);
			}
		}
	}
}
