package com.codeshaper.jello.engine.rendering.shader;

import com.google.gson.annotations.SerializedName;

public class ShaderData {

	public boolean depth_test = true;
	public CullMode culling = CullMode.BACK;
	
	public ShaderSource[] shaders = new ShaderSource[] {};

	public enum CullMode {
		@SerializedName("off")
		OFF,
		@SerializedName("back")
		BACK,
		@SerializedName("front")
		FRONT,
		@SerializedName("front_and_back")
		FRONT_AND_BACK
	}
}
