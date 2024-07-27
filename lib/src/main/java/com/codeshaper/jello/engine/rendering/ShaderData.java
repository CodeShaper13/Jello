package com.codeshaper.jello.engine.rendering;

import org.apache.commons.lang3.StringUtils;

public class ShaderData {

	public ShaderSource[] shaders = new ShaderSource[] {
			new ShaderSource(ShaderType.VERTEX, "code"),
			new ShaderSource(ShaderType.FRAGMENT, "code") };

	public static class ShaderSource {

		private ShaderType type;
		private String source;

		public ShaderSource() {
			this.type = ShaderType.UNKNOW;
			this.source = StringUtils.EMPTY;
		}

		public ShaderSource(ShaderType type, String source) {
			this.type = type;
			this.source = source;
		}

		public ShaderType getType() {
			return this.type;
		}

		public String getSource() {
			return this.source;
		}
	}
}
