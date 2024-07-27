package com.codeshaper.jello.engine.rendering;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.util.*;

import static org.lwjgl.opengl.GL30.*;

/**
 * Provides an interface for setting a shader programs uniforms.
 */
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
			System.out
					.println("Could not find uniform [" + uniformName + "] in shader program [" + this.programId + "]");
		}
		this.uniforms.put(uniformName, uniformLocation);
	}

	public void setUniform(String uniformName, int value) {
		glUniform1i(this.getUniformLocation(uniformName), value);
	}

	public void setUniform(String uniformName, float value) {
		glUniform1f(this.getUniformLocation(uniformName), value);
	}
	
    public void setUniform(String uniformName, Vector2f value) {
        glUniform2f(this.getUniformLocation(uniformName), value.x, value.y);
    }
	
    public void setUniform(String uniformName, Vector3f value) {
        glUniform3f(this.getUniformLocation(uniformName), value.x, value.y, value.z);
    }

    public void setUniform(String uniformName, Vector4f value) {
        glUniform4f(this.getUniformLocation(uniformName), value.x, value.y, value.z, value.w);
    }

	public void setUniform(String uniformName, Matrix4f value) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			glUniformMatrix4fv(this.getUniformLocation(uniformName), false, value.get(stack.mallocFloat(16)));
		}
	}

	private int getUniformLocation(String uniformName) {
		Integer location = this.uniforms.get(uniformName);
		if (location == null) {
			System.out.println("Could not find uniform [" + uniformName + "]");
		}
		return location.intValue();
	}
}