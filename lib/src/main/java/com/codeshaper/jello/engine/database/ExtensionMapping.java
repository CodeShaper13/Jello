package com.codeshaper.jello.engine.database;

import static org.reflections.scanners.Scanners.TypesAnnotated;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

import com.codeshaper.jello.editor.ScriptCompiler;
import com.codeshaper.jello.engine.AssetFileExtension;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.asset.Asset;
import com.codeshaper.jello.engine.asset.GenericAsset;

/**
 * Provides a mapping between file extensions and the {@link Assets} that
 * represents them.
 */
public class ExtensionMapping {

	// Separate lists, only the later has to rebuilt while the application is
	// running.
	private final HashMap<String, Class<Asset>> extensionToBuiltinAsset;
	private final HashMap<String, Class<Asset>> extensionTo3rdPartyAsset;

	public ExtensionMapping() {
		this.extensionToBuiltinAsset = new HashMap<String, Class<Asset>>();
		this.extensionTo3rdPartyAsset = new HashMap<String, Class<Asset>>();
		
		this.compileBuiltinMappings();
	}

	/**
	 * Gets the Asset class that provides the implementation of a file. If there is
	 * no Asset for the passed extension, a {@link GenericAsset} will be returned.
	 * 
	 * @param extension the files extensions, without the ".".
	 * @return An Asset class providing the implementation of a file.
	 */
	public Class<? extends Asset> getAssetClass(String extension) {
		Class<Asset> clazz = this.extensionTo3rdPartyAsset.get(extension);
		if (clazz != null) {
			return clazz;
		} else {
			// To user defined asset, check if there is a builtin provider class.
			clazz = this.extensionToBuiltinAsset.get(extension);
			if (clazz == null) {
				// Unknown extension.
				return GenericAsset.class;
			} else {
				return clazz;
			}
		}
	}
	
	public void compileProjectMappings(ScriptCompiler compiler) {
		// TODO
	}
	
	private void compileBuiltinMappings() {
		Reflections scan = new Reflections("com.codeshaper.jello.engine");

		Set<Class<?>> assetTypes = scan.get(TypesAnnotated.of(AssetFileExtension.class, AssetFileExtension.Internal.class).asClass());
		this.populateMapping(assetTypes, this.extensionToBuiltinAsset);
	}
	
	private void populateMapping(Set<Class<?>> classes, HashMap<String, Class<Asset>> mapping) {
		for (Class<?> cls : classes) {
			for (AssetFileExtension assetType : cls.getAnnotationsByType(AssetFileExtension.class)) {
				if (!Asset.class.isAssignableFrom(cls)) {
					Debug.logError("AssetFileExtention annotations are only allowed on subclasses of Asset");
					break;
				}
				
				int modifiers = cls.getModifiers();
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

				String extention = StringUtils.stripStart(assetType.value(), ".");
				
				@SuppressWarnings("unchecked")
				Class<Asset> castClass = (Class<Asset>) cls;
				
				mapping.put(extention, castClass);
			}
		}
	}
}
