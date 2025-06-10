package com.codeshaper.jello.engine;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to specify a custom name of a {@link JelloComponent}
 * in the inspector. This annotation has two primary uses:
 * <p>
 * 1). Giving the component a different name from it's Class name. This would be
 * useful if you wanted the component's name in the inspector to be more verbose
 * than it's class name, but be mindful not to create confusion when a user is
 * trying to reference it in a script. If they are not expecting the component
 * to have a different name that it's class, they may have a hard time finding
 * it.
 * <p>
 * 2). Defining the location of a component in the {@code Add Component} menu.
 * Components are sorted alphabetically in this menu, so to group like
 * components together, prefix them with their category. Ex.
 * {@code Rendering/Camera} and {@code Rendering/Mesh Renderer} will put the
 * components together.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ComponentName {

	/**
	 * The component's name
	 * @return the component's name
	 */
	public String value();
}
