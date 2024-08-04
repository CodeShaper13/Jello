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
		if (!glfwInit()) {
			Debug.logError("Unable to initialize GLFW.  Hard crash will likely follow...");
		}

		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);

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
			width = vidMode.width();
			height = vidMode.height();
		} else {
			this.width = appSettings.windowSize.x;
			this.height = appSettings.windowSize.y;
		}

		this.windowHandle = glfwCreateWindow(this.width, this.height, appSettings.windowTitle, NULL, NULL);
		if (this.windowHandle == NULL) {
			Debug.logError("Failed to create GLFW Window.  Hard crash will likely follow...");
		}

		// TODO set window icons.
		// glfwSetWindowIcon(this.windowHandle, JelloEditor.instance.assetDatabase);

		glfwSetFramebufferSizeCallback(windowHandle, (window, w, h) -> this.onWindowResize(w, h));
		glfwSetErrorCallback((errorCode, messagePointer) -> this.onError(errorCode, messagePointer));
		glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
			this.onKeyCallBack(key, action);
		});

		if (JelloEditor.instance == null) {
			// This is a build.
			glfwMakeContextCurrent(windowHandle);
		}

		if (appSettings.useVSync) {
			glfwSwapInterval(1);
		} else {
			glfwSwapInterval(0);
		}

		glfwShowWindow(windowHandle);

		// What does this do? Isn't width and height already set?
		int[] arrWidth = new int[1];
		int[] arrHeight = new int[1];
		glfwGetFramebufferSize(windowHandle, arrWidth, arrHeight);
		width = arrWidth[0];
		height = arrHeight[0];
	}

	public void cleanup() {
		Callbacks.glfwFreeCallbacks(windowHandle);
		glfwDestroyWindow(windowHandle);
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

	public void setSize(int width, int height) {
		throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}

	public boolean isFullscreen() {
		throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}

	public void setFullsreen(boolean fullscreen) {
		throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}

	private void onError(int errorCode, long msgPtr) {
		System.err.println(String.format("Error code [{}], msg [{}]", errorCode, MemoryUtil.memUTF8(msgPtr)));
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
