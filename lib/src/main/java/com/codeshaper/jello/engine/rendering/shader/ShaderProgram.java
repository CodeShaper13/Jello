package com.codeshaper.jello.engine.rendering.shader;

import static org.lwjgl.opengl.GL42.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL41;
import org.lwjgl.system.MemoryStack;

public class ShaderProgram {

	public final int programId;
	public final List<Uniform> uniforms;

	public ShaderProgram(ShaderSource... shaders) {
		this.uniforms = new ArrayList<Uniform>();

		this.programId = glCreateProgram();
		if (this.programId == 0) {
			throw new RuntimeException("Could not create Shader Program");
		}

		List<Integer> shaderModules = new ArrayList<>();
		for (ShaderSource source : shaders) {
			if (source != null) {
				int shaderId = this.createShader(source.source, source.type);
				shaderModules.add(shaderId);
			}
		}

		this.link(shaderModules);

		IntBuffer b0 = BufferUtils.createIntBuffer(1);
		IntBuffer b1 = BufferUtils.createIntBuffer(1);
		glGetProgramiv(this.programId, GL_ACTIVE_UNIFORMS, b0);
		int uniformCount = b0.get(0);
		for (int id = 0; id < uniformCount; id++) {
			String uniformName = glGetActiveUniform(this.programId, id, b0, b1);
			this.uniforms.add(new Uniform(id, uniformName, b1.get(0), b0.get(0)));
		}
	}

	public void bind() {
		glUseProgram(programId);
	}

	public void unbind() {
		glUseProgram(0);
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
		if (!doesUniformExist(uniformName)) {
			return;
		}
		glUniform4f(this.getUniformLocation(uniformName), value.x, value.y, value.z, value.w);
	}

	public void setUniform(String uniformName, Matrix4f value) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			glUniformMatrix4fv(this.getUniformLocation(uniformName), false, value.get(stack.mallocFloat(16)));
		}
	}
	

	/**
	 * Checks if a uniform exists in the program.
	 * 
	 * @param uniform the name of the uniform.
	 * @return {@code true} if the uniform exists.
	 */
	public boolean doesUniformExist(String uniform) {
		return this.getUniformLocation(uniform) >= 0;
	}

	public Collection<Uniform> getAllUniforms() {
		return Collections.unmodifiableCollection(this.uniforms);
	}
	
	public int getUniformLocation(String uniform) {
		return glGetUniformLocation(this.programId, uniform);
	}

	public void deleteProgram() {
		this.unbind();
		if (this.programId != 0) {
			glDeleteProgram(this.programId);
		}
		
	}

	private int createShader(String shaderCode, ShaderType shaderType) {
		int shaderId = glCreateShader(shaderType.type);

		if (shaderId == 0) {
			System.out.println("Error creating shader. Type: " + shaderType);
			return 0;
		}

		glShaderSource(shaderId, shaderCode);
		glCompileShader(shaderId);

		if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
			System.out.println("Error compiling Shader code: " + glGetShaderInfoLog(shaderId, 1024));
		}

		glAttachShader(programId, shaderId);

		return shaderId;
	}

	private void link(List<Integer> shaderModules) {
		glLinkProgram(programId);
		if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
			System.out.println("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));
		}

		shaderModules.forEach(s -> glDetachShader(programId, s));
		shaderModules.forEach(GL41::glDeleteShader);
	}
}
