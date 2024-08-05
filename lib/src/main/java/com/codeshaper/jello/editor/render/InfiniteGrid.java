package com.codeshaper.jello.editor.render;

import static org.lwjgl.opengl.GL20.*;

import org.joml.Matrix4f;

import com.codeshaper.jello.engine.component.Camera;
import com.codeshaper.jello.engine.rendering.GameRenderer;
import com.codeshaper.jello.engine.rendering.ShaderProgram;
import com.codeshaper.jello.engine.rendering.UniformsMap;

public class InfiniteGrid {

	private static final String NEAR = "near";
	private static final String FAR = "far";

	private ShaderProgram gridShaderProgram;
	private UniformsMap gridUniformsMap;

	public InfiniteGrid() {
		this.gridShaderProgram = new ShaderProgram(
				ShaderProgram.ShaderModuleData.fromResources("/editorGridShaders/editorGrid.vert",
						GL_VERTEX_SHADER),
				ShaderProgram.ShaderModuleData.fromResources("/editorGridShaders/editorGrid.frag",
						GL_FRAGMENT_SHADER));
		this.gridUniformsMap = new UniformsMap(gridShaderProgram.getProgramId());
		this.gridUniformsMap.createUniform(GameRenderer.PROJECTION_MATRIX);
		this.gridUniformsMap.createUniform(GameRenderer.VIEW_MATRIX);
		this.gridUniformsMap.createUniform(NEAR);
		this.gridUniformsMap.createUniform(FAR);
	}

	public void drawGrid(Camera camera, Matrix4f viewMatrix) {
		this.gridShaderProgram.bind();
		this.gridUniformsMap.setUniform(GameRenderer.PROJECTION_MATRIX, camera.getProjectionMatrix());
		this.gridUniformsMap.setUniform(GameRenderer.VIEW_MATRIX, viewMatrix);
		this.gridUniformsMap.setUniform(NEAR, camera.getNearPlane());
		this.gridUniformsMap.setUniform(FAR, camera.getFarPlane());

		glBegin(GL_QUADS);
		glVertex2f(-1f, -1f);
		glVertex2f(-1f, 1f);
		glVertex2f(1f, 1f);
		glVertex2f(1f, -1f);
		glEnd();

		this.gridShaderProgram.unbind();
	}
}
