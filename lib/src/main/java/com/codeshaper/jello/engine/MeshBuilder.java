package com.codeshaper.jello.engine;

import java.util.List;

import org.joml.Vector3f;

import com.codeshaper.jello.engine.asset.Mesh;

/**
 * Provides an interface for constructing Meshes at runtime.
 * <p>
 * After you are done making a mesh, apply it to a Mesh with {@link Mesh#apply(MeshBuilder)}}
 */
public class MeshBuilder {
	
	//private final List<Float> vertices;
	//private final List<Integer> indecs1;
	//private final List<Float> uv0;

	public final float[] verts;
	public final float[] textCoords;
	public final int[] indices;

	public MeshBuilder(float[] vertices, float[] textCoords, int[] indices) {
		this.verts = vertices;
		this.textCoords = textCoords;
		this.indices = indices;
	}
	
	/*
	public void addVertex(Vector3f vertex) {
		
	}
	
	public void removeVertex(int position) {
		
	}
	
	public void setVertices(List<Vector3f> vertices) {
		
	}
	*/
}
