package com.codeshaper.jello.engine.gui;

import static org.lwjgl.opengl.GL30.*;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import com.codeshaper.jello.editor.property.modifier.Space;
import com.codeshaper.jello.editor.property.modifier.ToolTip;
import com.codeshaper.jello.engine.Color;
import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.asset.Material;
import com.codeshaper.jello.engine.asset.Mesh;
import com.codeshaper.jello.engine.rendering.Camera;
import com.codeshaper.jello.engine.rendering.Renderer;
import com.codeshaper.jello.engine.rendering.shader.ShaderProgram;

public class UiBase extends Renderer {

	@ToolTip("The size of the UI Element in pixels.")
	public Vector2f size = new Vector2f(100, 100);

	public Anchor anchor = Anchor.MIDDLE;
	public Vector2f anchorPosition = new Vector2f(0.5f, 0.5f);

	@Space

	public Color color = Color.white;
	public Material material;

	@Override
	public Material getMaterial() {
		return this.material;
	}

	@Override
	public void onRender(Camera camera, float width, float height) {
		if (UiBase.uiMesh == null) {
			UiBase.createUiMesh();
		}

		if (this.material == null) {
			return;
		}

		ShaderProgram program = this.material.getShader().getProgram();

		float aspect = width / height;
		Matrix4f mat = new Matrix4f();
		mat.setOrtho(
				-(aspect) * height / 2, (aspect) * height / 2,
				-height / 2, height / 2,
				-100, 100);
		this.applyAnchor(mat, width, height);

		program.setUniform("_uiMatrix", mat);
		program.setUniform("_uiColor", this.color.toVector4f());
		program.setUniform("_size", new Vector3f(this.size.x, this.size.y, 0));

		glBindVertexArray(UiBase.uiMesh.getVaoId());
		glDrawElements(GL_TRIANGLES, UiBase.uiMesh.getVerticeCount(), GL_UNSIGNED_INT, 0);
	}

	public void applyAnchor(Matrix4f mat, float width, float height) {
		float w;
		float h;

		GameObject parent = this.gameObject().getParent();
		UiBase parentUiBase;
		if (parent != null) {
			parentUiBase = parent.getComponent(UiBase.class);
		} else {
			parentUiBase = null;
		}

		if (parent == null) {
			w = width / 2;
			h = height / 2;
		} else { // child
			UiBase uiBase = parent.getComponent(UiBase.class);
			w = uiBase.size.x / 2;
			h = uiBase.size.y / 2;
		}

		if (parent != null) {
			parentUiBase.applyAnchor(mat, width, height);
		}

		switch (this.anchor) {
		case TOP_LEFT:
			mat.translate(-w, h, 0);
			break;
		case TOP_MIDDLE:
			mat.translate(0, h, 0);
			break;
		case TOP_RIGHT:
			mat.translate(w, h, 0);
			break;
		case MIDDLE_LEFT:
			mat.translate(-w, 0, 0);
			break;
		case MIDDLE:
			mat.translate(0, 0, 0);
			break;
		case MIDDLE_RIGHT:
			mat.translate(w, 0, 0);
			break;
		case BOTTOM_LEFT:
			mat.translate(-w, -h, 0);
			break;
		case BOTTOM_MIDDLE:
			mat.translate(0, -h, 0);
			break;
		case BOTTOM_RIGHT:
			mat.translate(w, -h, 0);
			break;
		case CUSTOM:
			mat.translate(-w + (w * 2 * this.anchorPosition.x), -h + (h * 2 * this.anchorPosition.y), 0);
			break;
		default:
			break;
		}
	}

	private static Mesh uiMesh;

	private static void createUiMesh() {
		float[] positions = new float[] {
				-0.5f, 0.5f, 0.0f,
				-0.5f, -0.5f, 0.0f,
				0.5f, -0.5f, 0.0f,
				0.5f, 0.5f, 0.0f,
		};
		float[] uvs = new float[] {
				0, 0,
				0, 1,
				1, 1,
				1, 0
		};
		int[] indices = new int[] {
				0, 1, 2, 2, 3, 0,
		};

		uiMesh = new Mesh(positions, uvs, indices);
	}

	public enum Anchor {
		TOP_LEFT,
		TOP_MIDDLE,
		TOP_RIGHT,
		MIDDLE_LEFT,
		MIDDLE,
		MIDDLE_RIGHT,
		BOTTOM_LEFT,
		BOTTOM_MIDDLE,
		BOTTOM_RIGHT,
		CUSTOM;
	}
}