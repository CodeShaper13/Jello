package com.codeshaper.jello.engine;

import org.lwjgl.glfw.*;
import org.lwjgl.system.MemoryUtil;

import com.codeshaper.jello.editor.JelloEditor;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

	public final long windowHandle;

	/**
	 * Cached height of the window.
	 */
	private int height;
	/**
	 * Cached width of the window.
	 */
	private int width;

	Window(ApplicationSettings appSettings) {
		boolean isBuild = JelloEditor.instance == null;

		if (!glfwInit()) {
			Debug.logError("Unable to initialize GLFW.  Hard crash will likely follow...");
		}

		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, appSettings.isResizeable ? GL_TRUE : GL_FALSE);

		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
		if (appSettings.compatibleProfile) {
			glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_COMPAT_PROFILE);
		} else {
			glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
			glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
		}

		if (appSettings.fullscreen) {
			glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
			GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
			this.width = vidMode.width();
			this.height = vidMode.height();
		} else {
			this.width = appSettings.windowSize.x;
			this.height = appSettings.windowSize.y;
		}

		long monitor = appSettings.fullscreen ? glfwGetPrimaryMonitor() : NULL;
		this.windowHandle = glfwCreateWindow(this.width, this.height, appSettings.windowTitle, monitor, NULL);
		if (this.windowHandle == NULL) {
			Debug.logError("Failed to create GLFW Window.  Hard crash will likely follow...");
		}

		// TODO set window icons.
		// glfwSetWindowIcon(this.windowHandle, JelloEditor.instance.assetDatabase);

		glfwSetFramebufferSizeCallback(this.windowHandle, (window, w, h) -> this.onWindowResize(w, h));
		glfwSetErrorCallback((errorCode, messagePointer) -> this.onError(errorCode, messagePointer));
		glfwSetKeyCallback(this.windowHandle, (window, key, scancode, action, mods) -> {
			this.onKeyCallBack(key, action);
		});

		if (isBuild) {
			glfwMakeContextCurrent(this.windowHandle);
		}

		if (appSettings.useVSync) {
			glfwSwapInterval(1);
		} else {
			glfwSwapInterval(0);
		}

		glfwShowWindow(this.windowHandle);

		// Get the actual size of the window now that it's been created.
		int[] arrWidth = new int[1];
		int[] arrHeight = new int[1];
		glfwGetFramebufferSize(this.windowHandle, arrWidth, arrHeight);
		this.width = arrWidth[0];
		this.height = arrHeight[0];
	}

	public void cleanup() {
		Callbacks.glfwFreeCallbacks(this.windowHandle);
		glfwDestroyWindow(this.windowHandle);
		glfwTerminate();
		GLFWErrorCallback callback = glfwSetErrorCallback(null);
		if (callback != null) {
			callback.free();
		}
	}

	/**
	 * Gets the height on the window in pixels.
	 * 
	 * @return
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Gets the width of the window in pixels.
	 * 
	 * @return
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Sets the size of the window. If the window is in full screen mode, this sets
	 * the resolution of the window.
	 * 
	 * @param width  the new width of the window
	 * @param height the new height of the window
	 */
	public void setSize(int width, int height) {
		glfwSetWindowSize(this.windowHandle, width, height);
	}

	/**
	 * 
	 * @return {@code true} if the window is in full screen mode;
	 */
	public boolean isFullscreen() {
		throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}

	/**
	 * Sets if the window is full screen or not.
	 * 
	 * @param fullScreen should the windwow be in full screen mode?
	 */
	public void setFullsreen(boolean fullScreen) {
		long monitor = fullScreen ? glfwGetPrimaryMonitor() : NULL;
		glfwSetWindowMonitor(this.windowHandle, monitor, 0, 0, this.width, this.height, GL_DONT_CARE);
	}

	private void onError(int errorCode, long msgPtr) {
		System.err.println(String.format("Error code [%s], msg [%s]", errorCode, MemoryUtil.memUTF8(msgPtr)));
	}

	private void onWindowResize(int newWidth, int newHeight) {
		this.width = newWidth;
		this.height = newHeight;
	}

	private void onKeyCallBack(int key, int action) {
		if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
			glfwSetWindowShouldClose(windowHandle, true); // We will detect this in the rendering loop
		}
	}
}
