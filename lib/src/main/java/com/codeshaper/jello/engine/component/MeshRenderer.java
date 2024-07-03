package com.codeshaper.jello.engine.component;

import static org.lwjgl.opengl.GL30.*;

import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.asset.Material;
import com.codeshaper.jello.engine.asset.Mesh;

public class MeshRenderer extends JelloComponent {

	public Mesh mesh;
	public Material[] materials;

	public MeshRenderer(GameObject owner) {
		super(owner);
		
		float[] positions = new float[]{
                // VO
                -0.5f, 0.5f, 0.5f,
                // V1
                -0.5f, -0.5f, 0.5f,
                // V2
                0.5f, -0.5f, 0.5f,
                // V3
                0.5f, 0.5f, 0.5f,
                // V4
                -0.5f, 0.5f, -0.5f,
                // V5
                0.5f, 0.5f, -0.5f,
                // V6
                -0.5f, -0.5f, -0.5f,
                // V7
                0.5f, -0.5f, -0.5f,
        };
        float[] colors = new float[]{
                0.5f, 0.0f, 0.0f,
                0.0f, 0.5f, 0.0f,
                0.0f, 0.0f, 0.5f,
                0.0f, 0.5f, 0.5f,
                0.5f, 0.0f, 0.0f,
                0.0f, 0.5f, 0.0f,
                0.0f, 0.0f, 0.5f,
                0.0f, 0.5f, 0.5f,
        };
        int[] indices = new int[]{
                // Front face
                0, 1, 3, 3, 1, 2,
                // Top Face
                4, 0, 3, 5, 4, 3,
                // Right face
                3, 2, 7, 5, 3, 7,
                // Left face
                6, 1, 0, 6, 0, 4,
                // Bottom face
                2, 1, 6, 2, 6, 7,
                // Back face
                7, 6, 4, 7, 4, 5,
        };
        this.mesh = new Mesh(positions, colors, indices);
	}
	
	@Override
	public void onRender() {
		super.onRender();
						
		if (this.mesh != null) {
			glBindVertexArray(mesh.getVaoId());
            glDrawElements(GL_TRIANGLES, mesh.getNumVertices(), GL_UNSIGNED_INT, 0);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (this.mesh != null) {
			this.mesh.cleanup();
		}
	}
}
