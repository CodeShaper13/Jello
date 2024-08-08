package com.codeshaper.jello.engine.logging;

import java.util.Calendar;
import java.util.Date;

public class LogEntry {

	/**
	 * The type of log.
	 */
	public final LogType logType;
	public final String text;
	/**
	 * The object that triggered this log, or null if there was no object.
	 */
	public final Object context;
	/**
	 * The trace leading to the log. Null is allowed.
	 */
	public final String[] trace;
	/**
	 * The time this log happened. Null is allowed.
	 */
	public final Date time;

	public LogEntry(LogType logType, Object context, String text, String[] trace) {
		this(logType, context, text, trace, Calendar.getInstance().getTime());
	}

	public LogEntry(LogType logType, Object context, String text, String[] trace, Date time) {
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
