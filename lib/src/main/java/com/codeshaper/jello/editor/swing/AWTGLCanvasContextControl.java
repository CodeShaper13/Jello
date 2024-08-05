package com.codeshaper.jello.editor.swing;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.awt.AWTGLCanvas;

/**
 * Extension of {@link AWTGLCanvas} that provides more control over the canvas's
 * context.
 */
public abstract class AWTGLCanvasContextControl extends AWTGLCanvas {

	/**
	 * If {@link true} the canvas's context is kept current even after rendering.
	 */
	public boolean keepContextCurrent = false;

	public AWTGLCanvasContextControl() {
		super();
	}

	@Override
	protected void afterRender() {
		super.afterRender();

		if (this.keepContextCurrent) {
			this.platformCanvas.makeCurrent(this.context);
		}
	}

	/**
	 * Creates the canvas's context. Normally the canvas's context is created before
	 * it renders itself for the first time, but this can be called to create the
	 * context immediately.
	 */
	public void createContextImmediately() {
		if(this.context != 0) {
			System.err.println("Context already created!");
			return;
		}
		
		this.runInContext(() -> {
			GL.createCapabilities();
		});
	}

	/**
	 * Gets the canvas's context.
	 * 
	 * @return the canvas's context.
	 */
	public long getContext() {
		return this.context;
	}

	/**
	 * Sets the canvas's context. Warning, advanced users only!
	 * 
	 * @param context
	 */
	public void setContext(long context) {
		this.context = context;
	}
}
