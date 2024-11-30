package com.codeshaper.jello.editor;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import com.codeshaper.jello.editor.inspector.ComponentEditor;
import com.codeshaper.jello.editor.property.ExposedField;
import com.codeshaper.jello.editor.property.IExposedField;
import com.codeshaper.jello.editor.property.modifier.DisableIf;
import com.codeshaper.jello.editor.property.modifier.HideIf;
import com.codeshaper.jello.engine.ComponentIcon;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.JelloComponent;

public class EditorUtils {
	
	private static ImageIcon defaultComponentIcon = new ImageIcon(
			ComponentEditor.class.getResource(ComponentIcon.DEFAULT_ICON_PATH));

	private EditorUtils() {
	}

	public static String formatName(String string) {
		String[] words = StringUtils.splitByCharacterTypeCamelCase(string);
		return StringUtils.capitalize(String.join(" ", words));
	}

	/**
	 * Used by {@link DisableIf} and {@link HideIf}
	 * 
	 * @param exposedField
	 * @param line
	 * @return
	 */
	public static boolean evaluteLine(ExposedField exposedField, String line) {
		boolean invert;
		if (line.startsWith("!")) {
			invert = true;
			line = line.substring(1);
		} else {
			invert = false;
		}

		if (line.endsWith("()")) {
			// Method
			line = line.substring(0, line.length() - 2);
			try {
				Object returnValue = MethodUtils.invokeMethod(exposedField.instance, true, line);
				if (returnValue instanceof Boolean) {
					boolean b = (Boolean) returnValue;
					return invert ? !b : b;
				} else {
					Debug.logError(
							"Error drawing field \"%s\".  %s() must return a boolean.",
							func(exposedField),
							line);
				}
			} catch (NoSuchMethodException e) {
				Debug.logError(
						"Error drawing field \"%s\".  No method exists with the name \"%s\"",
						func(exposedField),
						line);
			} catch (IllegalAccessException e) {
				Debug.logError(
						"Error drawing field \"%s\". IllegalAccessException",
						func(exposedField),
						line);
			} catch (InvocationTargetException e) { // TODO print exception stack trace.
				Debug.logError(
						"Error drawing field \"%s\".  \"%s\" threw a %s exception",
						func(exposedField),
						line,
						e.getCause());
			}
		} else {
			// Field
			try {
				Field field = exposedField.instance.getClass().getField(line);
				field.setAccessible(true);
				Object value = field.get(exposedField.instance);
				if (value instanceof Boolean) {
					boolean b = (Boolean) value;
					return invert ? !b : b;
				} else {
					Debug.logError(
							"Error drawing field \"%s\".  \"%s\" must be of type boolean or Boolean",
							func(exposedField),
							line);
				}
			} catch (NoSuchFieldException e) {
				Debug.logError(
						"Error drawing field \"%s\".  No field exists with the name \"%s\"",
						func(exposedField),
						line);
			} catch (SecurityException e) {
				Debug.logError(
						"Error drawing field \"%s\".  Security Manager prevented \"%s\" from being accessed",
						func(exposedField),
						line);
			} catch (IllegalArgumentException e) { // Should never happen.
				Debug.logError(
						"Error drawing field \"%s\". IllegalArgumentException",
						func(exposedField),
						line);
			} catch (IllegalAccessException e) { // Should never happen.
				Debug.logError(
						"Error drawing field \"%s\". IllegalAccessException",
						func(exposedField),
						line);
			} catch (InaccessibleObjectException e) {
				Debug.logError(
						"Error drawing field \"%s\".  Unable to access \"%s\", try making it public",
						func(exposedField),
						line);
			}
		}

		return false;
	}
	
	public static Icon getComponentIcon(Class<JelloComponent> componentClass) {
		ComponentIcon annotation = componentClass.getAnnotation(ComponentIcon.class);
		if (annotation != null) {
			String path = annotation.value();
			URL url = ComponentEditor.class.getResource(path);
			if (url != null) {
				return new ImageIcon(url);
			} else {
				Debug.logError("Couldn't load component icon at ", path);
				return defaultComponentIcon;
			}
		} else {
			return defaultComponentIcon;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Icon getComponentIcon(JelloComponent component) {
		return getComponentIcon((Class<JelloComponent>) component.getClass());
	}
	
	private static String func(IExposedField field) {
		return field.getType().getSimpleName() + "#" + field.getFieldName();
	}
}
