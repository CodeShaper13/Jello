package com.codeshaper.jello.engine;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.codeshaper.jello.editor.inspector.Editor;

/**
 * This annotation is used to define a custom Editor for an {@link Asset} or
 * {@link JelloComponent}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CustomEditor {

	public Class<? extends Editor<?>> value();
}
