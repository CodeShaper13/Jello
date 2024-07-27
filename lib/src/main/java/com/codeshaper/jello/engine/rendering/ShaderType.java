package com.codeshaper.jello.engine.rendering;

import static org.lwjgl.opengl.GL43.*;

import com.google.gson.annotations.SerializedName;

public enum ShaderType {
	
	@SerializedName("vertex")
	VERTEX(GL_VERTEX_SHADER),
	@SerializedName("fragment")
	FRAGMENT(GL_FRAGMENT_SHADER),
	@SerializedName("geometry")
	GEOMETRY(GL_GEOMETRY_SHADER),
	@SerializedName("tess_control")
	TESS_CONTROL(GL_TESS_CONTROL_SHADER),
	@SerializedName("tess_eval")
	TESS_EVALUATION_SHADER(GL_TESS_EVALUATION_SHADER),
	@SerializedName("compute")
	COMPUTE_SHADER(GL_COMPUTE_SHADER),
	@SerializedName("unknown")
	UNKNOW(-1);

	public final int type;

	ShaderType(int type) {
		this.type = type;
	}
}
