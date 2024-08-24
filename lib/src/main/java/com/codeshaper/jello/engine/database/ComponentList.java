package com.codeshaper.jello.engine.database;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

import com.codeshaper.jello.editor.scripts.ScriptCompiler;
import com.codeshaper.jello.engine.JelloComponent;

/**
 * Provides a list of all Components that exists in the project. This list
 * contains both the builtin components and the components defined in the
 * project.
 */
public class ComponentList implements Iterable<Class<JelloComponent>> {

	// Separate lists, only the later has to rebuilt while the application is
	// running.
	private final List<Class<JelloComponent>> builtinComponents;
	private final List<Class<JelloComponent>> projectComponents;

	public ComponentList() {
		this.builtinComponents = this.getBuiltinComponents();
		this.projectComponents = new ArrayList<Class<JelloComponent>>();
	}
	
	public void compileProjectComponents(ScriptCompiler compiler) {
		this.projectComponents.clear();

		List<Class<JelloComponent>> classes = compiler.getAllScriptsOfType(JelloComponent.class);
		for (Class<JelloComponent> cls : classes) {
			if (isValidComponentClass(cls)) {
				this.projectComponents.add(cls);
			}
		}
	}

	/**
	 * Gets an iterator that goes through all of the components.
	 */
	public Iterator<Class<JelloComponent>> iterator() {
		return new ComponentIterator();
	}

	/**
	 * Gets a list of all of the builtin components.
	 * 
	 * @return a list of the builtin components.
	 */
	private List<Class<JelloComponent>> getBuiltinComponents() {
		List<Class<JelloComponent>> components = new ArrayList<Class<JelloComponent>>();
		Reflections scan = new Reflections("com.codeshaper.jello.engine");

		Set<Class<? extends JelloComponent>> classes = scan.getSubTypesOf(JelloComponent.class);
		for (var clazz : classes) {
			if (this.isValidComponentClass(clazz)) {
				@SuppressWarnings("unchecked")
				Class<JelloComponent> c = (Class<JelloComponent>) clazz;
				components.add(c);
			}
		}

		return components;
	}

	/**
	 * Checks if {@code cls} is a valid Component class. For a class to be a valid
	 * component, it must be a public, not an interface, not abstract, and not a
	 * native class.
	 * 
	 * @param cls the cls to check.
	 * @return {@code true} if the Class is a valid component class.
	 */
	private boolean isValidComponentClass(Class<?> cls) {
		int modifiers = cls.getModifiers();
		if (Modifier.isInterface(modifiers)) {
			return false;
		}
		if (Modifier.isAbstract(modifiers)) {
			return false;
		}
		if (Modifier.isNative(modifiers)) {
			return false;
		}
		if (!Modifier.isPublic(modifiers)) {
			return false;
		}

		return true;
	}

	private class ComponentIterator implements Iterator<Class<JelloComponent>> {

		private int index = 0;

		public boolean hasNext() {
			return index < this.getCount();
		}

		public Class<JelloComponent> next() {
			Class<JelloComponent> result;

			int builtinComponentCount = builtinComponents.size();
			if (this.index < builtinComponentCount) {
				result = builtinComponents.get(index);
			} else {
				result = projectComponents.get(index - builtinComponentCount);
			}

			index++;
			return result;
		}

		private int getCount() {
			return builtinComponents.size() + projectComponents.size();
		}
	}
}
