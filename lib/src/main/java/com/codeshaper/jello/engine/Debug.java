package com.codeshaper.jello.engine;

import java.util.Arrays;

import org.joml.Vector3d;

import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.engine.logging.ILogHandler;
import com.codeshaper.jello.engine.logging.LogType;

public class Debug {
	
	public static void log(String message, Object... args) {
		Debug.log(LogType.NORMAL, null, message, args);
	}

	public static void log(GameObject context, String message, Object... args) {
		Debug.log(LogType.NORMAL, context, message, args);
	}
	
	public static void logWarning(String message, Object... args) {
		Debug.log(LogType.WARNING, null, message, args);
	}

	public static void logWarning(GameObject context, String message, Object... args) {
		Debug.log(LogType.WARNING, context, message, args);
	}
	
	public static void logError(String message, Object... args) {
		Debug.log(LogType.ERROR, null, message, args);
	}

	public static void logError(GameObject context, String message, Object... args) {
		Debug.log(LogType.ERROR, context, message, args);
	}
	
	public static void drawLine(Vector3d start, Vector3d end, Color color) {
		// TODO
	}
	
	public static void drawRay(Vector3d start, Vector3d direction, Color color) {
		// TODO
	}
	
	public static void log(LogType logType, GameObject context, String message, Object... args) {
		ILogHandler handler = JelloEditor.instance.logHandler;
		if(handler != null) {
			StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
			if(stackTrace.length > 3) {
				// Trim the first 3 elements.
				stackTrace = Arrays.copyOfRange(stackTrace, 2, stackTrace.length);
			}
			handler.log(logType, context, String.format(message, args), stackTrace);
		}
	}
}
