package com.codeshaper.jello.engine.component;

import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.asset.Material;

/**
 * An abstract base class that lets a component render itself to the display.
 */
public abstract class Renderer extends JelloComponent {

	public Renderer(GameObject owner) {
		super(owner);
	}

	/**
	 * Gets the {@link Material} to render this object with. If no material is returned,
	 * this object will not get rendered and {@link Renderer#onRender()} will not be
	 * called.
	 * 
	 * @return the {@link Material} to render with.
	 */
	public abstract Material getMaterial();

	/**
	 * Called to actually render the component.
	 */
	public abstract void onRender();
}
