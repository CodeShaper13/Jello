package com.codeshaper.jello.editor.property.modifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Hides a field if a method evaluates to true.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DisableIf {

	/**
	 * The name of a method that is used to determine if the field should be enabled
	 * or not. The method must have no arguments and return a boolean indicating if
	 * the field should be disabled (true) or not (false). Disabled fields are not
	 * interactable and act as if they have the {@link ReadOnly} annotation.
	 * 
	 * @return the name of the method.
	 */
	public String value();
}
