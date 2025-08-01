package com.codeshaper.jello.engine;

import org.lwjgl.glfw.GLFW;

/**
 * Used by {@link Input#setCursorState(CursorState)} to set the mouse's state.
 */
public enum CursorState {

	/**
	 * The cursor is visible and unrestricted. This is the default state.
	 */
	NORMAL(GLFW.GLFW_CURSOR_NORMAL),
	/**
	 * The cursor is visible, and confined to the window.
	 */
	CAPTURED(GLFW.GLFW_CURSOR_CAPTURED),
	/**
	 * The cursor is invisible when it is over the window, but free to leave the
	 * window.
	 * <p>
	 * This is useful if you have a custom cursor and you don't want the normal cursor to be visible.
	 */
	HIDDEN(GLFW.GLFW_CURSOR_HIDDEN),
	/**
	 * The cursor is invisible and stuck to the middle of the screen.
	 * <p>
	 * This is useful for FPS style games where the mouse provides input, but is not
	 * visible and can't leave the screen.
	 */
	DISABLED(GLFW.GLFW_CURSOR_DISABLED);

	public final int value;

	private CursorState(int value) {
		this.value = value;
	}
}
