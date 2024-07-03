package com.codeshaper.jello.editor.property.modifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Creates a button in the inspector that when clicked will run the function.
 * The functions must have no parameters, or an error will be logged.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Button {

	/**
	 * @return The text to display for the button.
	 */
	public String value() default "";
}
