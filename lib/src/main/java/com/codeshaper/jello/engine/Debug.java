package com.codeshaper.jello.engine;

import java.io.PrintStream;
import java.util.Arrays;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joml.Vector3d;
import org.joml.Vector3f;

import com.codeshaper.jello.editor.GizmoDrawer;
import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.window.ConsoleWindow;
import com.codeshaper.jello.engine.logging.ILogHandler;
import com.codeshaper.jello.engine.logging.LogEntry;
import com.codeshaper.jello.engine.logging.LogType;

/**
 * Provides a collection methods for debugging your application. None of these
 * methods will have any effect and minimal overhead in builds, so they can be
 * safely left in production code.
 */
public final class Debug {

	private Debug() {
	}

	/**
	 * Asserts a condition and if the condition fails, an error is logged.
	 * <p>
	 * If called without the Editor running, nothing happens.
	 * 
	 * @param condition  a condition expected to be true
	 * @param message    the message to show in the console
	 * @param formatArgs optional arguments if the {@code message} is a formatted
	 *                   string
	 */
	public static void asset(boolean condition, String message, Object... formatArgs) {
		if (!isInEditor()) {
			return;
		}

		if (!condition) {
			logError(message, formatArgs);
		}
	}

	/**
	 * Logs a normal message to the console.
	 * <p>
	 * If called without the Editor running, nothing happens.
	 * 
	 * @param message    the message to show in the console
	 * @param formatArgs optional arguments if the {@code message} is a formatted
	 *                   string
	 */
	public static void log(String message, Object... formatArgs) {
		if (!isInEditor()) {
			return;
		}
		internalLog(LogType.NORMAL, null, message, formatArgs);
	}

	/**
	 * Logs a normal message to the console with a context.
	 * <p>
	 * If called without the Editor running, nothing happens.
	 * 
	 * @param context    the object that caused this log
	 * @param message    the message to show in the console
	 * @param formatArgs optional arguments if the {@code message} is a formatted
	 *                   string
	 */
	public static void logWithContext(JelloObject context, String message, Object... formatArgs) {
		if (!isInEditor()) {
			return;
		}
		internalLog(LogType.NORMAL, context, message, formatArgs);
	}

	/**
	 * Logs a warning message to the console.
	 * <p>
	 * If called without the Editor running, nothing happens.
	 * 
	 * @param message    the message to show in the console
	 * @param formatArgs optional arguments if the {@code message} is a formatted
	 *                   string
	 */
	public static void logWarning(String message, Object... formatArgs) {
		if (!isInEditor()) {
			return;
		}
		internalLog(LogType.WARNING, null, message, formatArgs);
	}

	/**
	 * Logs a warning message to the console with a context.
	 * <p>
	 * If called without the Editor running, nothing happens.
	 * 
	 * @param context    the object that caused this log
	 * @param message    the message to show in the console
	 * @param formatArgs optional arguments if the {@code message} is a formatted
	 *                   string
	 */
	public static void logWarningWithContext(JelloObject context, String message, Object... formatArgs) {
		if (!isInEditor()) {
			return;
		}
		internalLog(LogType.WARNING, context, message, formatArgs);
	}

	/**
	 * Logs an error message to the console.
	 * <p>
	 * If called without the Editor running, nothing happens.
	 * 
	 * @param message    the message to show in the console
	 * @param formatArgs optional arguments if the {@code message} is a formatted
	 *                   string
	 */
	public static void logError(String message, Object... formatArgs) {
		if (!isInEditor()) {
			return;
		}
		internalLog(LogType.ERROR, null, message, formatArgs);
	}

	/**
	 * Logs an error message to the console with a context.
	 * <p>
	 * If called without the Editor running, nothing happens.
	 * 
	 * @param context    the object that caused this log
	 * @param message    the message to show in the console
	 * @param formatArgs optional arguments if the {@code message} is a formatted
	 *                   string
	 */
	public static void logErrorWithContext(JelloObject context, String message, Object... formatArgs) {
		if (!isInEditor()) {
			return;
		}
		internalLog(LogType.ERROR, context, message, formatArgs);
	}

	/**
	 * Logs a message to the console.
	 * <p>
	 * If called without the Editor running, nothing happens.
	 * 
	 * @param logType    the type of the log
	 * @param context    the object that caused this log
	 * @param message    the message to show in the console
	 * @param formatArgs optional arguments if the {@code message} is a formatted
	 *                   string
	 */
	public static void log(LogType logType, JelloObject context, String message, Object... formatArgs) {
		if (!isInEditor()) {
			return;
		}
		internalLog(logType, context, message, formatArgs);
	}

	/**
	 * Logs an exception and it's stack trace to the console.
	 * <p>
	 * If called without the Editor running, nothing happens.
	 * 
	 * @param exception the exception to log
	 */
	public static void log(Throwable exception) {
		if (!isInEditor()) {
			return;
		}
		log(exception, null);
	}

	/**
	 * Logs an exception to the console with a context associated with it.
	 * <p>
	 * If called without the Editor running, nothing happens.
	 * 
	 * @param context   the object that threw the exception.
	 * @param exception the exception to log.
	 */
	public static void log(Throwable exception, JelloObject context) {
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
	 * <p>
	 * If called without the Editor running, nothing happens.
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
	 * <p>
	 * If called without the Editor running, nothing happens.
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

	/**
	 * Clears the console.
	 * <p>
	 * If called without the Editor running, nothing happens.
	 */
	public static void clearConsole() {
		if (!isInEditor()) {
			return;
		}

		ConsoleWindow console = JelloEditor.getWindow(ConsoleWindow.class);
		if (console != null) {
			console.clear();
		}
	}

	/**
	 * Checks if the Editor is running. In builds, this will return {@code false}.
	 * 
	 * @return {@code true} if the Editor is running
	 */
	public static boolean isInEditor() {
		return JelloEditor.instance != null;
	}

	private static void internalLog(LogType logType, JelloObject context, String message, Object... formatArgs) {
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

	private static ILogHandler getLogHandler() {
		if (Debug.isInEditor()) {
			ILogHandler editorLogHandler = JelloEditor.instance.logHandler;
			if (editorLogHandler == null) {
				return new StandardLog();
			} else {
				return editorLogHandler;
			}
		} else {
			return null;
		}
	}

	private static class StandardLog implements ILogHandler {

		@Override
		public void log(LogEntry entry) {
			PrintStream printStream = entry.logType == LogType.ERROR ? System.err : System.out;

			String contextArg = entry.context != null ? entry.context.toString() : StringUtils.EMPTY;
			String line = String.format("[Jello.Debug]:%s %s", contextArg, entry.text);
			printStream.println(line);
		}

	}
}
