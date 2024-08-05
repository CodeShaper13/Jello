package com.codeshaper.jello.engine.rendering;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class ShaderSource {

	private final ShaderType type;
	private final String source;

	public ShaderSource() {
		this.type = ShaderType.UNKNOW;
		this.source = StringUtils.EMPTY;
	}

	public ShaderSource(String location, ShaderType type) {
		String source;
		try (InputStream stream = ShaderSource.class.getResourceAsStream(location)) {
			source = IOUtils.toString(stream, StandardCharsets.UTF_8);			
		} catch(IOException e) {
			e.printStackTrace();
			source = StringUtils.EMPTY;
		}
		
		this.source = source;
		this.type = type;
	}

	public ShaderType getType() {
		return this.type;
	}

	public String getSource() {
		return this.source;
	}
}
