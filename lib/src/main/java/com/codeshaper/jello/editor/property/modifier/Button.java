package com.codeshaper.jello.editor.property.modifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.codeshaper.jello.editor.EditorUtils;

/**
 * Creates a button in the inspector that when clicked will invoke the decorated
 * field. The method can have any access modifier. The method must be
 * non-static, and have no parameters, or an error will be logged.
 * <p>
 * The button's label will be the name of the decorated method, formated with
 * {@link EditorUtils#formatName(String)}. This default behavior can be
 * overriden by specifying the text to place on the button with
 * {@link Button#value()}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Button {

	/**
	 * @return The text to display for the button. If not set, the decorated
	 *         method's name is used.
	 */
	public String value() default "";
}
