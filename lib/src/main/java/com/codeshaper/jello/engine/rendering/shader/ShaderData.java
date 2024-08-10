package com.codeshaper.jello.engine.rendering.shader;

public class ShaderData {

	public boolean depth_test = true;
	public CullMode culling = CullMode.BACK;
	
	public ShaderSource[] shaders = new ShaderSource[] {};

	public enum CullMode {
		OFF,
		BACK,
		FRONT,
		FRONT_AND_BACK
	}
}
