package com.codeshaper.jello.engine;

import java.nio.DoubleBuffer;

import org.joml.Vector2d;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWScrollCallbackI;

public class Input {

	public static int LEFT_MOUSE_BUTTON = GLFW.GLFW_MOUSE_BUTTON_LEFT;
	public static int RIGHT_MOUSE_BUTTON = GLFW.GLFW_MOUSE_BUTTON_RIGHT;
	public static int MIDDLE_MOUSE_BUTTON = GLFW.GLFW_MOUSE_BUTTON_MIDDLE;

	private static Input instance;

	private long windowHandle;
	private DoubleBuffer mouseXBuffer;
	private DoubleBuffer mouseYBuffer;
	private Vector2d mouseScroll;

	private Input(long windowHandle) {
		Input.instance = this;

		this.windowHandle = windowHandle;
		this.mouseXBuffer = BufferUtils.createDoubleBuffer(1);
		this.mouseYBuffer = BufferUtils.createDoubleBuffer(1);
		this.mouseScroll = new Vector2d(0, 0);

		GLFW.glfwSetScrollCallback(windowHandle, new GLFWScrollCallbackI() {
			@Override
			public void invoke(long window, double xoffset, double yoffset) {
				mouseScroll = new Vector2d(xoffset, yoffset);
			}
		});
	}

	static void initialize(long windowHandle) {
		new Input(windowHandle);
	}

	static void onEndOfFrame() {
		instance.mouseScroll = new Vector2d(0, 0);
	}

	public static boolean isKeyPressed(int key) {
		return GLFW.glfwGetKey(instance.windowHandle, key) == GLFW.GLFW_PRESS;
	}

	public static boolean isKeyReleased(int key) {
		return GLFW.glfwGetKey(instance.windowHandle, key) == GLFW.GLFW_RELEASE;
	}

	/**
	 * Gets the position of the mouse relative to the top left of the screen.
	 * 
	 * @return A {@link Vector2d} holding the mouse position.
	 */
	public static Vector2d getMousePos() {
		GLFW.glfwGetCursorPos(instance.windowHandle, instance.mouseXBuffer, instance.mouseYBuffer);
		return new Vector2d(instance.mouseXBuffer.get(0), instance.mouseYBuffer.get(0));
	}

	/**
	 * Sets the position of the cursor. If the window is not in focus, nothing
	 * happens.
	 * 
	 * @param x The x position of the cursor, relative to the top left of the
	 *          screen.
	 * @param y The y position of the cursor, relative to the top left of the
	 *          screen.
	 */
	public static void setMousePos(double x, double y) {
		GLFW.glfwSetCursorPos(instance.windowHandle, x, y);
	}

	public static boolean isMouseButtonPressed(int button) {
		return GLFW.glfwGetMouseButton(instance.windowHandle, 0) == GLFW.GLFW_PRESS;
	}

	public static boolean isMouseButtonReleased(int button) {
		return GLFW.glfwGetMouseButton(instance.windowHandle, 0) == GLFW.GLFW_RELEASE;
	}

	public static Vector2d getMouseScroll() {
		return instance.mouseScroll;
	}

	public static EnumCursorMode getCursorState() {
		int value = GLFW.glfwGetInputMode(instance.windowHandle, GLFW.GLFW_CURSOR);
		switch (value) {
		case GLFW.GLFW_CURSOR_NORMAL:
			return EnumCursorMode.NORMAL;
		case GLFW.GLFW_CURSOR_CAPTURED:
			return EnumCursorMode.CAPTURED;
		case GLFW.GLFW_CURSOR_HIDDEN:
			return EnumCursorMode.HIDDEN;
		case GLFW.GLFW_CURSOR_DISABLED:
			return EnumCursorMode.DISABLED;
		default:
			return EnumCursorMode.NORMAL; // TODO what should be returned on "error", and can this even happen.
		}
	}

	public static void setCursorState(EnumCursorMode cursorMode) {
		GLFW.glfwSetInputMode(instance.windowHandle, GLFW.GLFW_CURSOR, cursorMode.value);
	}
}
