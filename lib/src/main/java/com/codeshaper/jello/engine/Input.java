package com.codeshaper.jello.engine;

import java.nio.DoubleBuffer;
import java.util.Arrays;

import org.joml.Vector2i;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Provides a collection of static methods for gathering input from the keyboard
 * and mouse. This is not suitable for input gathering within the Editor,
 * instead normal Swing methods should be used.
 */
public class Input {

	/**
	 * The id of the left mouse button.
	 */
	public static final int LEFT_MOUSE_BUTTON = GLFW.GLFW_MOUSE_BUTTON_LEFT;
	/**
	 * The id of the right mouse button.
	 */
	public static final int RIGHT_MOUSE_BUTTON = GLFW.GLFW_MOUSE_BUTTON_RIGHT;
	/**
	 * The id of the middle mouse button.
	 */
	public static final int MIDDLE_MOUSE_BUTTON = GLFW.GLFW_MOUSE_BUTTON_MIDDLE;

	private static final int MOUSE_BUTTON_COUNT = 8;

	private static Input instance;

	private final long window;
	private final DoubleBuffer mouseXBuffer;
	private final DoubleBuffer mouseYBuffer;
	private final KeyCode[] keyCodeValues;

	private ButtonPressState[] keyStates;
	private ButtonPressState[] mouseBtnStates;
	private int mouseScroll;

	private Input(long windowHandle) {
		this.window = windowHandle;
		this.mouseXBuffer = BufferUtils.createDoubleBuffer(1);
		this.mouseYBuffer = BufferUtils.createDoubleBuffer(1);
		this.keyCodeValues = KeyCode.values();

		this.keyStates = new ButtonPressState[this.keyCodeValues.length];
		this.mouseBtnStates = new ButtonPressState[MOUSE_BUTTON_COUNT];
		Arrays.fill(this.keyStates, ButtonPressState.NEITHER);
		Arrays.fill(this.mouseBtnStates, ButtonPressState.NEITHER);

		glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
			int index = -1;
			for (int i = 0; i < this.keyCodeValues.length; i++) {
				if (this.keyCodeValues[i].code == key) {
					index = i;
					break;
				}
			}

			if (index != -1) {
				if (action == GLFW_PRESS || action == GLFW_RELEASE)
					this.keyStates[index] = action == GLFW_PRESS ? ButtonPressState.PRESSED : ButtonPressState.RELEASED;
			} else {
				Debug.logWarning("Unknown key was pressed with an id of %s", key);
			}
		});

		glfwSetMouseButtonCallback(windowHandle, (window, button, action, mods) -> {
			this.mouseBtnStates[button] = action == GLFW_PRESS ? ButtonPressState.PRESSED : ButtonPressState.RELEASED;
		});

		glfwSetScrollCallback(windowHandle, (window, x, y) -> {
			this.mouseScroll = (int) y;
		});
	}

	static void initialize(long window) {
		if (instance != null) {
			Debug.logError("Input has already been initialized");
			return;
		}

		instance = new Input(window);
	}

	static void disable() {
		instance = null;
	}

	static void onEndOfFrame() {
		Arrays.fill(instance.keyStates, ButtonPressState.NEITHER);
		Arrays.fill(instance.mouseBtnStates, ButtonPressState.NEITHER);
		instance.mouseScroll = 0;
	}

	/**
	 * Checks if a key was pressed this frame.
	 * 
	 * @param key the key to check
	 * @return {@code true} if the key was pressed this frame
	 */
	public static boolean isKeyPressed(KeyCode key) {
		if (!isEnabled()) {
			return false;
		}

		if (key == null) {
			return false;
		}

		return instance.keyStates[key.ordinal()] == ButtonPressState.PRESSED;
	}

	/**
	 * Checks if a key was released this frame.
	 * 
	 * @param key the key to check
	 * @return {@code true} if the key was released this frame
	 */
	public static boolean isKeyReleased(KeyCode key) {
		if (!isEnabled()) {
			return false;
		}

		if (key == null) {
			return false;
		}

		return instance.keyStates[key.ordinal()] == ButtonPressState.RELEASED;
	}

	/**
	 * Checks if a key is currently down.
	 * 
	 * @param key the key to check
	 * @return {@code true} if the key is currently down
	 */
	public static boolean isKeyDown(KeyCode key) {
		if (!isEnabled()) {
			return false;
		}

		if (key == null) {
			return false;
		}

		return GLFW.glfwGetKey(instance.window, key.code) == GLFW.GLFW_RELEASE;
	}

	/**
	 * Gets the position of the cursor in pixel coordinates relative to the top left
	 * of the screen. If the cursor is outside of the screen, negative numbers or
	 * numbers greater than the size of the screen will be returned.
	 * 
	 * @return A {@link Vector2i} holding the cursor's position.
	 */
	public static Vector2i getMousePos() {
		if (!isEnabled()) {
			return new Vector2i(0, 0);
		}

		GLFW.glfwGetCursorPos(instance.window, instance.mouseXBuffer, instance.mouseYBuffer);
		return new Vector2i(
				(int) Math.floor(instance.mouseXBuffer.get(0)),
				(int) Math.floor(instance.mouseYBuffer.get(0)));
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
	public static void setMousePos(int x, int y) {
		if (!isEnabled()) {
			return;
		}

		GLFW.glfwSetCursorPos(instance.window, x, y);
	}

	/**
	 * Checks if a mouse button was pressed this frame.
	 * 
	 * @param button the id of the button to check. 0 = left, 1 = right, 2 = middle
	 * @return {@code true} if the button was pressed.
	 * @see Input#LEFT_MOUSE_BUTTON
	 * @see Input#RIGHT_MOUSE_BUTTON
	 * @see Input#MIDDLE_MOUSE_BUTTON
	 */
	public static boolean isMouseButtonPressed(int button) {
		if (!isEnabled()) {
			return false;
		}

		if (button < 0 || button >= MOUSE_BUTTON_COUNT) {
			return false;
		}

		return instance.mouseBtnStates[button] == ButtonPressState.PRESSED;
	}

	/**
	 * Checks if a mouse button was released this frame.
	 * 
	 * @param button the id of the button to check. 0 = left, 1 = right, 2 = middle
	 * @return {@code true} if the button was released.
	 * @see Input#LEFT_MOUSE_BUTTON
	 * @see Input#RIGHT_MOUSE_BUTTON
	 * @see Input#MIDDLE_MOUSE_BUTTON
	 */
	public static boolean isMouseButtonReleased(int button) {
		if (!isEnabled()) {
			return false;
		}

		if (button < 0 || button >= MOUSE_BUTTON_COUNT) {
			return false;
		}

		return instance.mouseBtnStates[button] == ButtonPressState.RELEASED;
	}

	/**
	 * Checks if a mouse button is currently down.
	 * 
	 * @param button the id of the button to check. 0 = left, 1 = right, 2 = middle
	 * @return {@code true} if the button is down.
	 * @see Input#LEFT_MOUSE_BUTTON
	 * @see Input#RIGHT_MOUSE_BUTTON
	 * @see Input#MIDDLE_MOUSE_BUTTON
	 */
	public static boolean isMouseButtonDown(int button) {
		if (!isEnabled()) {
			return false;
		}

		if (button < 0 || button >= MOUSE_BUTTON_COUNT) {
			return false;
		}

		return GLFW.glfwGetMouseButton(instance.window, button) == GLFW.GLFW_PRESS;
	}

	/**
	 * Gets how much the scroll wheel has moved this frame. Positive values mean the
	 * scroll wheel has moved up, and negative values mean it has moved down. 0
	 * means the scroll wheel has not moved this frame.
	 * 
	 * @return how much the scroll wheel moved this frame.
	 */
	public static int getMouseScroll() {
		if (!isEnabled()) {
			return 0;
		}

		return instance.mouseScroll;
	}

	/**
	 * Sets Application's the cursor state. {@code null} will never be returned.
	 * 
	 * @return the Application's cursor state.
	 */
	public static CursorState getCursorState() {
		if (!isEnabled()) {
			return CursorState.NORMAL;
		}

		int value = GLFW.glfwGetInputMode(instance.window, GLFW.GLFW_CURSOR);
		switch (value) {
		case GLFW.GLFW_CURSOR_NORMAL:
			return CursorState.NORMAL;
		case GLFW.GLFW_CURSOR_CAPTURED:
			return CursorState.CAPTURED;
		case GLFW.GLFW_CURSOR_HIDDEN:
			return CursorState.HIDDEN;
		case GLFW.GLFW_CURSOR_DISABLED:
			return CursorState.DISABLED;
		default:
			return CursorState.NORMAL;
		}
	}

	/**
	 * Gets the Application's {@link CursorState}. If {@code null} is passed,
	 * nothing happens.
	 * 
	 * @param state
	 */
	public static void setCursorState(CursorState state) {
		if (!isEnabled()) {
			return;
		}

		GLFW.glfwSetInputMode(instance.window, GLFW.GLFW_CURSOR, state.value);
	}

	/**
	 * Checks if the Input System is enabled and gathering input, thus can be
	 * polled. In the Editor, the Input System is only enabled in Play Mode. In
	 * builds, it is always enabled.
	 * 
	 * @return {@code true} if the Input System is enabled.
	 */
	public static boolean isEnabled() {
		return instance != null;
	}

	private enum ButtonPressState {
		NEITHER,
		PRESSED,
		RELEASED,
	}
}
