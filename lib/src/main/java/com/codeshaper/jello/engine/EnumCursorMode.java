package com.codeshaper.jello.engine;

import org.lwjgl.glfw.GLFW;

/**
 * Used by {@link Input#setCursorState(EnumCursorMode)}.
 */
public enum EnumCursorMode {

	/**
	 * 
	 */
	NORMAL(GLFW.GLFW_CURSOR_NORMAL),
	/**
	 * 
	 */
	CAPTURED(GLFW.GLFW_CURSOR_CAPTURED),
	/**
	 * 
	 */
	HIDDEN(GLFW.GLFW_CURSOR_HIDDEN),
	/**
	 * 
	 */
	DISABLED(GLFW.GLFW_CURSOR_DISABLED);

	public final int value;

	EnumCursorMode(int value) {
		this.value = value;
	}
}
