package com.codeshaper.jello.engine;

import java.util.Arrays;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joml.Vector3d;
import org.joml.Vector3f;

import com.codeshaper.jello.editor.GizmoDrawer;
import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.engine.asset.Asset;
import com.codeshaper.jello.engine.component.JelloComponent;
import com.codeshaper.jello.engine.logging.ILogHandler;
import com.codeshaper.jello.engine.logging.LogEntry;
import com.codeshaper.jello.engine.logging.LogType;

/**
 * {@link Debug} provides a collection methods for debugging your application.
 * None of these methods will have any effect and minimal overhead in builds, so
 * they can be safely left in production code.
 */
public class Debug {

	public static void log(String message, Object... formatArgs) {
		if (!isInEditor()) {
			return;
		}
		internalLog(LogType.NORMAL, null, message, formatArgs);
	}

	public static void logWithContext(Object context, String message, Object... formatArgs) {
		if (!isInEditor()) {
			return;
		}
		internalLog(LogType.NORMAL, context, message, formatArgs);
	}

	public static void logWarning(String message, Object... formatArgs) {
		if (!isInEditor()) {
			return;
		}
		internalLog(LogType.WARNING, null, message, formatArgs);
	}

	public static void logWarningWithContext(Object context, String message, Object... formatArgs) {
		if (!isInEditor()) {
			return;
		}
		internalLog(LogType.WARNING, context, message, formatArgs);
	}

	public static void logError(String message, Object... formatArgs) {
		if (!isInEditor()) {
			return;
		}
		internalLog(LogType.ERROR, null, message, formatArgs);
	}

	public static void logErrorWithContext(Object context, String message, Object... formatArgs) {
		if (!isInEditor()) {
			return;
		}
		internalLog(LogType.ERROR, context, message, formatArgs);
	}

	public static void log(LogType logType, Object context, String message, Object... formatArgs) {
		if (!isInEditor()) {
			return;
		}
		internalLog(logType, context, message, formatArgs);
	}

	/**
	 * Logs an exception to the console.
	 * 
	 * @param exception the exception to log.
	 */
	public static void log(Exception exception) {
		if (!isInEditor()) {
			return;
		}
		log(exception, null);
	}

	/**
	 * Logs an exception to the console with a context associated with it. The
	 * context should be the object that threw this exception. The context can be a
	 * {@link GameObject}, {@link JelloComponent}, {@link Asset} or null if there is
	 * no associated object.
	 * 
	 * @param context   the object that threw the exception.
	 * @param exception the exception to log.
	 */
	public static void log(Exception exception, Object context) {
		if (!isInEditor()) {
			return;
		}
		ILogHandler handler = getLogHandler();
		if (handler != null) {
			String[] frames = ExceptionUtils.getStackFrames(exception);
			LogEntry entry = new LogEntry(LogType.ERROR, context, exception.toString(), frames);
			handler.log(entry);
		}
	}

	/**
	 * Draws a debug line. Unlike Gizmos drawn with the {@link GizmoDrawer}, these
	 * lines are visible in the game view.
	 * 
	 * @param start the start of the line in world space.
	 * @param end   the end of the line is world space.
	 * @param color the color of the line.
	 */
	public static void drawLine(Vector3f start, Vector3f end, Color color) {
		if (!isInEditor()) {
			return;
		}
		throw new NotImplementedException(); // TODO
	}

	/**
	 * Draws a debug ray. Unlike Gizmos drawn with the {@link GizmoDrawer}, these
	 * lines are visible in the game view.
	 * 
	 * @param start     the start on the ray in world space.
	 * @param direction the direction of the ray in world space. The ray's
	 *                  {@link Vector3d#length()} is the length of the ray.
	 * @param color     the color of the line.
	 */
	public static void drawRay(Vector3f start, Vector3f direction, Color color) {
		if (!isInEditor()) {
			return;
		}
		throw new NotImplementedException(); // TODO
	}

	private static void internalLog(LogType logType, Object context, String message, Object... formatArgs) {
		ILogHandler handler = getLogHandler();
		if (handler != null) {
			StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
			if (stackTrace.length > 4) {
				// Trim the first 3 elements.
				stackTrace = Arrays.copyOfRange(stackTrace, 3, stackTrace.length);
			}
			
			String[] s = new String[stackTrace.length];
			for (int i = 0; i < stackTrace.length; i++) {
				s[i] = stackTrace[i].toString();
			}
			
			LogEntry entry = new LogEntry(logType, context, String.format(message, formatArgs), s);
			handler.log(entry);
		}
	}

	private static boolean isInEditor() {
		return JelloEditor.instance != null;
	}

	private static ILogHandler getLogHandler() {
		return Debug.isInEditor() ? JelloEditor.instance.logHandler : null;
	}
}
