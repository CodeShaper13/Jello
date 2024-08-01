package com.codeshaper.jello.editor.property.modifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Creates empty space above a field.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Space {

	/**
	 * The height of the space in pixels.
	 */
	int value() default 10;
}
