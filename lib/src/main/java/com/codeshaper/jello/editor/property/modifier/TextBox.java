package com.codeshaper.jello.editor.property.modifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Makes a string field display as a multi-line textbox instead of a single line
 * entry field. This annotation has no effect on non-string field.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TextBox {

	/**
	 * The number of lines in the textbox.
	 */
	public int value() default 1;
}
