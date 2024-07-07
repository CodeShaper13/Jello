package com.codeshaper.jello.engine;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Repeatable(AssetFileExtension.Internal.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AssetFileExtension {

	/**
	 * Returns the file extension specified. There may or may not be a leading ".",
	 * both are allowed.
	 * 
	 * @return the extension.
	 */
	public String value() default "";

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface Internal {
		AssetFileExtension[] value();
	}
}
