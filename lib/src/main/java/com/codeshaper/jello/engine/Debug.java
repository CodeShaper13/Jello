package com.codeshaper.jello.engine;

import java.util.Arrays;

import org.joml.Vector3d;

import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.engine.logging.ILogHandler;
import com.codeshaper.jello.engine.logging.LogEntry;
import com.codeshaper.jello.engine.logging.LogType;

public class Debug {
	
	public static void log(String message, Object... args) {
		Debug.internalLog(LogType.NORMAL, null, message, args);
	}

	public static void logWithContext(Object context, String message, Object... args) {
		Debug.internalLog(LogType.NORMAL, context, message, args);
	}
	
	public static void logWarning(String message, Object... args) {
		Debug.internalLog(LogType.WARNING, null, message, args);
	}

	public static void logWarningWithContext(Object context, String message, Object... args) {
		Debug.internalLog(LogType.WARNING, context, message, args);
	}
	
	public static void logError(String message, Object... args) {
		Debug.internalLog(LogType.ERROR, null, message, args);
	}

	public static void logErrorWithContext(Object context, String message, Object... args) {
		Debug.internalLog(LogType.ERROR, context, message, args);
	}
	
	public static void log(LogType logType, Object context, String message, Object... args) {
		Debug.internalLog(logType, context, message, args);
	}
	
	public static void drawLine(Vector3d start, Vector3d end, Color color) {
		// TODO
	}
	
	public static void drawRay(Vector3d start, Vector3d direction, Color color) {
		// TODO
	}
	
	private static void internalLog(LogType logType, Object context, String message, Object... args) {
		ILogHandler handler = JelloEditor.instance != null ? JelloEditor.instance.logHandler : null;
		if(handler != null) {
			StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
			if(stackTrace.length > 4) {
				// Trim the first 3 elements.
				stackTrace = Arrays.copyOfRange(stackTrace, 3, stackTrace.length);
			}
			LogEntry entry = new LogEntry(logType, context, String.format(message, args), stackTrace);
			handler.log(entry);
		}
	}
}
