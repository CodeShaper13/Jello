package com.codeshaper.jello.editor.property.modifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adds a tool tip to a field. 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ToolTip {

	/**
	 * The text to display in the tool tip.  If null, no tool tip will be shown.
	 * 
	 * @return the tooltip's text.
	 */
	public String value();
}
