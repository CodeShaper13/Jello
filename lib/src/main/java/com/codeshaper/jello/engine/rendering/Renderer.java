package com.codeshaper.jello.engine.rendering;

import com.codeshaper.jello.engine.JelloComponent;
import com.codeshaper.jello.engine.asset.Material;

/**
 * An abstract base class that lets a component render itself to the display.
 */
public abstract class Renderer extends JelloComponent {

	/**
	 * Gets the {@link Material} to render this object with. If no material is
	 * returned, this object will not get rendered and {@link Renderer#onRender()}
	 * will not be called.
	 * 
	 * @return the {@link Material} to render with.
	 */
	public abstract Material getMaterial();

	/**
	 * Called to render this Component. Implementing classes will define this
	 * behavior. In situations where there are multiple Cameras, this method will be
	 * called multiple times on the same Component instance, once for each Camera.
	 * 
	 * @param camera the {@link Camera} that is rendering this Component
	 */
	public abstract void onRender(Camera camera);
}
