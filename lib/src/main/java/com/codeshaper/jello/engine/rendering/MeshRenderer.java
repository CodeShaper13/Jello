package com.codeshaper.jello.engine.rendering;

import static org.lwjgl.opengl.GL30.*;

import com.codeshaper.jello.engine.ComponentIcon;
import com.codeshaper.jello.engine.ComponentName;
import com.codeshaper.jello.engine.asset.Material;
import com.codeshaper.jello.engine.asset.Mesh;

/**
 * MeshRenderer components render meshes to the display.
 */
@ComponentName("Rendering/Mesh Renderer")
@ComponentIcon("/_editor/componentIcons/meshRenderer.png")
public final class MeshRenderer extends Renderer {

	public Mesh mesh;
	public Material material;

	@Override
	public Material getMaterial() {
		return this.material;
	}
	
	@Override
	public void onRender(Camera camera) {
		if (this.mesh != null) {
			glBindVertexArray(mesh.getVaoId());
			glDrawElements(GL_TRIANGLES, mesh.getVerticeCount(), GL_UNSIGNED_INT, 0);
		}
	}
}
