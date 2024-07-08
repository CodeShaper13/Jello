package com.codeshaper.jello.engine.logging;

import java.util.Calendar;
import java.util.Date;

public class LogEntry {

	/**
	 * The type of log.
	 */
	public final LogType logType;
	/**
	 * The object that triggered this log. Valid objects are instances
	 * of GameObject, Component, and Asset.  Null is allowed.
	 * 
	 * NOT YET IMPLEMENTED!
	 */
	public final Object context;
	public final String text;
	/**
	 * The trace leading to the log.  Null is allowed.
	 */
	public final StackTraceElement[] trace;
	/**
	 * The time this log was logged.  Null is allowed.
	 */
	public final Date time;

	public LogEntry(LogType logType, Object context, String text, StackTraceElement[] trace) {
		this(logType, context, text, trace, Calendar.getInstance().getTime());
	}

	public LogEntry(LogType logType, Object context, String text, StackTraceElement[] trace, Date time) {
		this.logType = logType;
		this.context = context;
		this.text = text;
		this.trace = trace;
		this.time = time;
	}
	
	@Override
	public String toString() {
		return text;
	}
}
