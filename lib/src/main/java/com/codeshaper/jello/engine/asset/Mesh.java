package com.codeshaper.jello.engine.asset;

import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

import com.codeshaper.jello.engine.AssetFileExtension;
import com.codeshaper.jello.engine.AssetLocation;
import com.codeshaper.jello.engine.MeshBuilder;
import com.codeshaper.jello.engine.ModelLoader;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL30.*;

@AssetFileExtension(".fbx")
@AssetFileExtension(".obj")
@AssetFileExtension(".blend")
public class Mesh extends Asset {
	
	private int numVertices;
	private int vaoId;
	private List<Integer> vboIdList;	

	public Mesh(AssetLocation location) {
		super(location);
	}

	public Mesh(float[] positions, float[] textCoords, int[] indices) {
		super(null);
		
		this.constructMesh(positions, textCoords, indices);
	}

	@Override
	public void load() {
		MeshBuilder data = ModelLoader.loadModel(location);
		this.constructMesh(data.verts, data.textCoords, data.indices);
	}
	
	private void constructMesh(float[] positions, float[] textCoords, int[] indices) {		
		try (MemoryStack stack = MemoryStack.stackPush()) {
			this.numVertices = indices.length;
			vboIdList = new ArrayList<>();

			vaoId = glGenVertexArrays();
			glBindVertexArray(vaoId);

			// Positions VBO
			int vboId = glGenBuffers();
			vboIdList.add(vboId);
			FloatBuffer positionsBuffer = stack.callocFloat(positions.length);
			positionsBuffer.put(0, positions);
			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, positionsBuffer, GL_STATIC_DRAW);
			glEnableVertexAttribArray(0);
			glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

			// Texture coordinates VBO
			vboId = glGenBuffers();
			vboIdList.add(vboId);
			FloatBuffer textCoordsBuffer = stack.callocFloat(textCoords.length);
			textCoordsBuffer.put(0, textCoords);
			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, textCoordsBuffer, GL_STATIC_DRAW);
			glEnableVertexAttribArray(1);
			glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

			// Index VBO
			vboId = glGenBuffers();
			vboIdList.add(vboId);
			IntBuffer indicesBuffer = stack.callocInt(indices.length);
			indicesBuffer.put(0, indices);
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboId);
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

			glBindBuffer(GL_ARRAY_BUFFER, 0);
			glBindVertexArray(0);
		}
	}

	@Override
	public void unload() {
		this.vboIdList.forEach(GL30::glDeleteBuffers);
		glDeleteVertexArrays(this.vaoId);
	}

	/**
	 * Gets the number of vertices in the mesh.
	 * 
	 * @return
	 */
	public int getVerticeCount() {
		return this.numVertices;
	}

	/**
	 * Gets the Mesh's Vertex Attribute Object id.
	 * 
	 * @return
	 */
	public final int getVaoId() {
		return this.vaoId;
	}
	
	/**
	 * Uploads the changes to the Mesh to the GPU.
	 */
	public void apply() {
		
	}
}