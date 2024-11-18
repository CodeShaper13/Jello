package com.codeshaper.jello.engine;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides a link to an online help resource for the Component. When present,
 * the help button is enabled in the components header and will open
 * {@link value} as a URL in the System's web browser.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ComponentHelpUrl {

	/**
	 * The help URL to online documentation for the component.
	 */
	public String value() default "https://www.google.com/search?q=jello component"; // TODO
}