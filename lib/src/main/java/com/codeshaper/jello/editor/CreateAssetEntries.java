package com.codeshaper.jello.editor;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import com.codeshaper.jello.editor.event.ProjectReloadListener;
import com.codeshaper.jello.editor.property.modifier.CreateAssetEntry;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.asset.SerializedJelloObject;

/**
 * Provides a list of Assets types that can be created via the Create menu in
 * the File Browser.
 */
public class CreateAssetEntries implements Iterable<CreateAssetEntries.MenuEntry>, ProjectReloadListener {

	private List<MenuEntry> builtinEntries;
	private List<MenuEntry> createAssetEntries;

	public CreateAssetEntries() {
		this.builtinEntries = new ArrayList<MenuEntry>();

		Reflections scan = new Reflections("com.codeshaper.jello.engine");
		Set<Class<?>> createAssetEntries = scan.get(Scanners.TypesAnnotated.of(CreateAssetEntry.class).asClass());
		for (Class<?> clazz : createAssetEntries) {
			CreateAssetEntry annotation = clazz.getAnnotation(CreateAssetEntry.class);

			if (this.isClassValid(clazz)) {
				MenuEntry data = new MenuEntry(annotation, (Class<? extends SerializedJelloObject>) clazz);
				this.builtinEntries.add(data);
			}
		}

		this.createAssetEntries = new ArrayList<>();

		JelloEditor.instance.addProjectReloadListener(this);
	}

	@Override
	public Iterator<MenuEntry> iterator() {
		return this.createAssetEntries.iterator();
	}

	@Override
	public void onProjectReload(Phase phase) {
		if(phase == Phase.REBUILD) {
			this.createAssetEntries.clear();

			// Add builtin Assets
			for (MenuEntry entry : this.builtinEntries) {
				this.createAssetEntries.add(entry);
			}

			// Scan for and add project Assets.

			// TODO
		}
	}

	private boolean isClassValid(Class<?> clazz) {
		int modifiers = clazz.getModifiers();
		if (Modifier.isAbstract(modifiers)) {
			Debug.logError("[Asset Database]: CreateAssetEntry annotations are not allowed on abstract classes.");
			return false;
		}
		if (Modifier.isInterface(modifiers)) {
			Debug.logError("[Asset Database]: CreateAssetEntry annotations are not allowed on interfaces.");
			return false;
		}
		if (clazz.isMemberClass() && !Modifier.isStatic(modifiers)) {
			Debug.logError(
					"[Asset Database]: CreateAssetEntry annotations are not allowed on non-static inner classes.");
		}
		if (!SerializedJelloObject.class.isAssignableFrom(clazz)) {
			Debug.logError(
					"[Asset Database]: CreateAssetEntry annotations are only allowed on subclasses of SerializedJelloObject.");
			return false;
		}

		return true;
	}

	public class MenuEntry {

		private final String fileName;
		private final String menuName;
		public final Class<? extends SerializedJelloObject> clazz;

		public MenuEntry(CreateAssetEntry annotation, Class<? extends SerializedJelloObject> clazz) {
			this.fileName = annotation.fileName();
			this.menuName = annotation.location();
			this.clazz = clazz;
		}

		public String getMenuName() {
			if (StringUtils.isBlank(this.menuName)) {
				return this.clazz.getName();
			} else {
				return this.menuName;
			}
		}

		/**
		 * Gets the Assets default name without it's extension.
		 * 
		 * @return
		 */
		public String getNewAssetName() {
			String fileName;
			if (StringUtils.isBlank(this.fileName)) {
				fileName = this.clazz.getName().toLowerCase();
			} else {
				fileName = this.fileName;
			}
			return fileName;
		}
	}

}
