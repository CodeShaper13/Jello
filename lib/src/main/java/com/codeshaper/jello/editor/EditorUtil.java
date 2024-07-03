package com.codeshaper.jello.editor;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang3.reflect.MethodUtils;

import com.codeshaper.jello.editor.property.ExposedField;
import com.codeshaper.jello.engine.Debug;

public class EditorUtil {

	public static boolean invokeMethod(ExposedField exposedField, String methodName)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		try {
			Object returnValue = MethodUtils.invokeMethod(exposedField.instance, true, methodName);
			if (returnValue instanceof Boolean) {
				if (((Boolean) returnValue) == true) {
					return true; // Hide the field.
				}
			} else {
				Debug.logError("Error drawing field \"%s\".  %s() must return a boolean.", exposedField.getFieldName(),
						methodName);
			}
		} catch (NoSuchMethodException e) {
			Debug.logError(
					"Error drawing field \"%s\".  A zero argument method with the name \"%s\" could not be found.",
					exposedField.getFieldName(), methodName);
		}

		return false;
	}
}
