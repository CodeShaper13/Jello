package com.codeshaper.jello.editor.property.modifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Hides a field if a field or method evaluates to true.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface HideIf {

	/**
	 * The name of a method that is used to determine if the field should be hidden
	 * or not. The method must have no arguments and a return a boolean indicating
	 * if the field should be hidden (true) or visible (false).
	 * 
	 * @return the name of the method.
	 */
	public String value();
}
