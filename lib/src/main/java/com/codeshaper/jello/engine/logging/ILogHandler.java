package com.codeshaper.jello.engine.logging;

import com.codeshaper.jello.engine.GameObject;

public interface ILogHandler {

	public void log(LogType logType, GameObject context, String text, StackTraceElement[] args);
}
