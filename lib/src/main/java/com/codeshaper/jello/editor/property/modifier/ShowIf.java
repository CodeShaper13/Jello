package com.codeshaper.jello.editor.property.modifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Shows a filed only if a method evaluates to true.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ShowIf {
	
	/**
	 * The name of a method that is used to determine if the field should be shown
	 * or not. The method must have no arguments and a return a boolean indicating
	 * if the field should be shown (true) or hidden (false).
	 * 
	 * @return the name of the method.
	 */
	public String value();
}
