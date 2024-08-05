package com.codeshaper.jello.editor.render;

import static org.lwjgl.opengl.GL20.*;

import org.joml.Matrix4f;

import com.codeshaper.jello.engine.component.Camera;
import com.codeshaper.jello.engine.rendering.GameRenderer;
import com.codeshaper.jello.engine.rendering.ShaderProgram;

public class InfiniteGrid {

	private static final String NEAR = "near";
	private static final String FAR = "far";

	private ShaderProgram shaderProgram;

	public InfiniteGrid() {
		this.shaderProgram = new ShaderProgram(
				ShaderProgram.ShaderModuleData.fromResources("/editorGridShaders/editorGrid.vert",
						GL_VERTEX_SHADER),
				ShaderProgram.ShaderModuleData.fromResources("/editorGridShaders/editorGrid.frag",
						GL_FRAGMENT_SHADER));
		this.shaderProgram.createUniform(GameRenderer.PROJECTION_MATRIX);
		this.shaderProgram.createUniform(GameRenderer.VIEW_MATRIX);
		this.shaderProgram.createUniform(NEAR);
		this.shaderProgram.createUniform(FAR);
	}

	public void drawGrid(Camera camera, Matrix4f viewMatrix) {
		this.shaderProgram.bind();
		this.shaderProgram.setUniform(GameRenderer.PROJECTION_MATRIX, camera.getProjectionMatrix());
		this.shaderProgram.setUniform(GameRenderer.VIEW_MATRIX, viewMatrix);
		this.shaderProgram.setUniform(NEAR, camera.getNearPlane());
		this.shaderProgram.setUniform(FAR, camera.getFarPlane());

		glBegin(GL_QUADS);
		glVertex2f(-1f, -1f);
		glVertex2f(-1f, 1f);
		glVertex2f(1f, 1f);
		glVertex2f(1f, -1f);
		glEnd();

		this.shaderProgram.unbind();
	}
}