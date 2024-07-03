package com.codeshaper.jello.engine.logging;

import java.io.IOException;
import java.io.OutputStream;

public class LoggerOutputStream extends OutputStream {

	private final ILogHandler logHandler;
	private final LogType logType;

	public LoggerOutputStream(ILogHandler logHandler, LogType logType) {
		this.logHandler = logHandler;
		this.logType = logType;
	}

	@Override
	public void write(byte[] buffer, int offset, int length) throws IOException {
		String text = new String(buffer, offset, length);
		this.logHandler.log(this.logType, null, text, null);
	}

	@Override
	public void write(int b) throws IOException {
		this.write(new byte[] { (byte) b }, 0, 1);
	}
}
