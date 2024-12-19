package com.codeshaper.jello.engine;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to specify the icon of a {@link JelloComponent} in
 * the inspector.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ComponentIcon {

	public static final String DEFAULT_ICON_PATH = "/_editor/componentIcons/default.png";

	public String value() default DEFAULT_ICON_PATH;
}
