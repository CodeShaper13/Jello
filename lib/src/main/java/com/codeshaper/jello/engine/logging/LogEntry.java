package com.codeshaper.jello.engine.logging;

import com.codeshaper.jello.engine.GameObject;

public class LogEntry {

	public final LogType logType;
	public final GameObject context;
	public final String text;
	public final StackTraceElement[] trace;

	public LogEntry(LogType logType, GameObject context, String text, StackTraceElement[] trace) {
		this.logType = logType;
		this.context = context;
		this.text = text;
		this.trace = trace;
	}

	@Override
	public String toString() {
		return text;
	}
}
