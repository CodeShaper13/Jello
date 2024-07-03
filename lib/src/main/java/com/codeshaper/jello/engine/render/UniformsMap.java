package com.codeshaper.jello.engine.render;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.util.*;

import static org.lwjgl.opengl.GL20.*;

public class UniformsMap {

	private int programId;
	private Map<String, Integer> uniforms;

	public UniformsMap(int programId) {
		this.programId = programId;
		this.uniforms = new HashMap<>();
	}

	public void createUniform(String uniformName) {
		int uniformLocation = glGetUniformLocation(this.programId, uniformName);
		if (uniformLocation < 0) {
			throw new RuntimeException(
					"Could not find uniform [" + uniformName + "] in shader program [" + this.programId + "]");
		}
		this.uniforms.put(uniformName, uniformLocation);
	}

	public void setUniform(String uniformName, Matrix4f value) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			Integer location = this.uniforms.get(uniformName);
			if (location == null) {
				throw new RuntimeException("Could not find uniform [" + uniformName + "]");
			}
			glUniformMatrix4fv(location.intValue(), false, value.get(stack.mallocFloat(16)));
		}
	}
}