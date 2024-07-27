package com.codeshaper.jello.engine;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ComponentIcon {
	
	public static final String DEFAULT_ICON_PATH = "/editor/componentIcons/default.png";

	public String value() default DEFAULT_ICON_PATH;
}
