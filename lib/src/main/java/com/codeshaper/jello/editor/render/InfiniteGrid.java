package com.codeshaper.jello.editor.render;

import static org.lwjgl.opengl.GL20.*;

import org.joml.Matrix4f;

import com.codeshaper.jello.engine.component.Camera;
import com.codeshaper.jello.engine.rendering.GameRenderer;
import com.codeshaper.jello.engine.rendering.ShaderProgram;
import com.codeshaper.jello.engine.rendering.ShaderSource;
import com.codeshaper.jello.engine.rendering.ShaderType;

public class InfiniteGrid {

	private static final String NEAR = "near";
	private static final String FAR = "far";

	private ShaderProgram shaderProgram;

	public InfiniteGrid() {
		this.shaderProgram = new ShaderProgram(
				new ShaderSource("/editorGridShaders/editorGrid.vert",
						ShaderType.VERTEX),
				new ShaderSource("/editorGridShaders/editorGrid.frag",
						ShaderType.FRAGMENT));
	}

	public void drawGrid(Camera camera, Matrix4f viewMatrix) {
		this.shaderProgram.bind();
		this.shaderProgram.setUniform(GameRenderer.PROJECTION_MATRIX, camera.getProjectionMatrix());
		this.shaderProgram.setUniform(GameRenderer.VIEW_MATRIX, viewMatrix);
		this.shaderProgram.setUniform(NEAR, camera.getNearPlane());
		this.shaderProgram.setUniform(FAR, camera.getFarPlane());

		glDisable(GL_CULL_FACE);
		
		glBegin(GL_QUADS);
		glVertex2f(-1f, -1f);
		glVertex2f(-1f, 1f);
		glVertex2f(1f, 1f);
		glVertex2f(1f, -1f);
		glEnd();

		this.shaderProgram.unbind();
	}
}
