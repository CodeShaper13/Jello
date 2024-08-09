package com.codeshaper.jello.engine.rendering;

public class Uniform {

	/**
	 * The index of the uniform.
	 */
	public final int index;
	/**
	 * The type of the uniform, or null if the type is unknown.
	 */
	public final UniformType type;
	/**
	 * The name of the uniform.
	 */
	public final String name;
	/**
	 * The size of the uniform. For non-array uniforms this is 1, for array uniforms
	 * it is the number of elements in the array.
	 */
	public final int size;

	public Uniform(int id, String name, int type, int size) {
		this.index = id;
		this.type = UniformType.from(type);
		this.name = name;
		this.size = size;
	}

	@Override
	public String toString() {
		return String.format("Uniform {id: %s, name: %s, type: %s}", this.index, this.name, this.type);
	}
}
