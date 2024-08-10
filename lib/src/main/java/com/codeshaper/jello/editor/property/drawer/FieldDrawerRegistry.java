package com.codeshaper.jello.editor.property.drawer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.event.ProjectReloadListener.Phase;
import com.codeshaper.jello.engine.Debug;

public class FieldDrawerRegistry {

	private HashMap<Class<?>, FieldDrawer> builtinDrawers;
	private HashMap<Class<?>, FieldDrawer> projectDefinedDrawers;

	public FieldDrawerRegistry(JelloEditor editor) {
		this.builtinDrawers = new HashMap<Class<?>, FieldDrawer>();
		this.builtinDrawers = new HashMap<Class<?>, FieldDrawer>();

		editor.addProjectReloadListener((phase) -> this.onProjectReload(phase));

		Reflections scan = new Reflections("com.codeshaper.jello.editor");
		Set<Class<?>> classes = scan
				.get(Scanners.TypesAnnotated.of(FieldDrawerType.class, FieldDrawerType.Internal.class).asClass());
		this.populateMapping(classes, this.builtinDrawers);
	}

	/**
	 * Gets a {@link FieldDrawer} for {@code type} If no {@link FieldDrawer}
	 * exists for the {@code type}, {@code null} is returned.
	 * 
	 * @param type the type to get a {@link FieldDrawer} for.
	 * @return a {@link FieldDrawer}.
	 */
	public FieldDrawer getDrawer(Class<?> type) {
		FieldDrawer drawer = this.checkMap(type, this.builtinDrawers);
		if (builtinDrawers != null) {
			return drawer;
		} else {
			return this.checkMap(type, this.projectDefinedDrawers);
		}
	}

	private void onProjectReload(Phase phase) {
		if (phase == Phase.POST_REBUILD) {
			// TODO
		}
	}
	
	private void populateMapping(Set<Class<?>> drawerClasses, HashMap<Class<?>, FieldDrawer> map) {
		for (Class<?> drawerClass : drawerClasses) {
			for (FieldDrawerType targetTypeAnnotation : drawerClass.getAnnotationsByType(FieldDrawerType.class)) {
				int modifiers = drawerClass.getModifiers();
				if (Modifier.isAbstract(modifiers)) {
					Debug.logError("FieldDrawerType annotations are not allowed on abstract classes.");
					break;
				}
				if (Modifier.isInterface(modifiers)) {
					Debug.logError("FieldDrawerType annotations are not allowed on interfaces");
					break;
				}
				if (!FieldDrawer.class.isAssignableFrom(drawerClass)) {
					Debug.logError("FieldDrawerType annotations are only allowed on subclasses of FieldDrawer");
					break;
				}

				try {
					Class<?> drawerTargetClass = targetTypeAnnotation.value();
					Constructor<?> ctor = drawerClass.getConstructor();
					FieldDrawer drawer = (FieldDrawer) ctor.newInstance();
					map.put(drawerTargetClass, drawer);
				} catch (NoSuchMethodException | SecurityException e) {
					Debug.logError(
							"Classse with the FieldDrawerType annotations must have a public, no argument constructor");
					break;
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private FieldDrawer checkMap(Class<?> clazz, HashMap<Class<?>, FieldDrawer> map) {
		for (var entry : map.entrySet()) {
			if (entry.getKey().isAssignableFrom(clazz)) {
				return entry.getValue();
			}
		}
		return null;
	}
}
