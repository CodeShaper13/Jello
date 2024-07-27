package com.codeshaper.jello.engine;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIVector3D;
import static org.lwjgl.assimp.Assimp.*;

public class ModelLoader {

	private ModelLoader() {
	}

	public static MeshBuilder loadModel(AssetLocation location) {
		return loadModel(location,
				aiProcess_GenSmoothNormals | aiProcess_JoinIdenticalVertices | aiProcess_Triangulate
						| aiProcess_FixInfacingNormals | aiProcess_CalcTangentSpace | aiProcess_LimitBoneWeights
						| aiProcess_PreTransformVertices);

	}

	public static MeshBuilder loadModel(AssetLocation location, int flags) {
		//String texturePath = path.toString();
		//try (InputStream stream = texturePath.startsWith("builtin")
		//		? ModelLoader.class.getResourceAsStream("/" + texturePath)
		//		: new FileInputStream(texturePath)) {
		try (InputStream stream = location.getInputSteam()) {
			byte[] byteArray = IOUtils.toByteArray(stream);
			ByteBuffer buffer = BufferUtils.createByteBuffer(byteArray.length);
	        buffer.put(byteArray);
	        buffer.flip();
			
			AIScene aiScene = aiImportFileFromMemory(buffer, flags, "");

			if (aiScene == null) {
				System.out.println("Error loading model: " + aiGetErrorString());
			}

			PointerBuffer aiMeshes = aiScene.mMeshes();
			AIMesh aiMesh = AIMesh.create(aiMeshes.get(0));
			return processMesh(aiMesh);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	private static int[] processIndices(AIMesh aiMesh) {
		List<Integer> indices = new ArrayList<>();
		int numFaces = aiMesh.mNumFaces();
		AIFace.Buffer aiFaces = aiMesh.mFaces();
		for (int i = 0; i < numFaces; i++) {
			AIFace aiFace = aiFaces.get(i);
			IntBuffer buffer = aiFace.mIndices();
			while (buffer.remaining() > 0) {
				indices.add(buffer.get());
			}
		}
		return indices.stream().mapToInt(Integer::intValue).toArray();
	}

	private static MeshBuilder processMesh(AIMesh aiMesh) {
		float[] vertices = processVertices(aiMesh);
		float[] textCoords = processTextCoords(aiMesh);
		int[] indices = processIndices(aiMesh);

		// Texture coordinates may not have been populated. We need at least the empty
		// slots
		if (textCoords.length == 0) {
			int numElements = (vertices.length / 3) * 2;
			textCoords = new float[numElements];
		}

		return new MeshBuilder(vertices, textCoords, indices);
	}

	private static float[] processTextCoords(AIMesh aiMesh) {
		AIVector3D.Buffer buffer = aiMesh.mTextureCoords(0);
		if (buffer == null) {
			return new float[] {};
		}
		float[] data = new float[buffer.remaining() * 2];
		int pos = 0;
		while (buffer.remaining() > 0) {
			AIVector3D textCoord = buffer.get();
			data[pos++] = textCoord.x();
			data[pos++] = 1 - textCoord.y();
		}
		return data;
	}

	private static float[] processVertices(AIMesh aiMesh) {
		AIVector3D.Buffer buffer = aiMesh.mVertices();
		float[] data = new float[buffer.remaining() * 3];
		int pos = 0;
		while (buffer.remaining() > 0) {
			AIVector3D textCoord = buffer.get();
			data[pos++] = textCoord.x();
			data[pos++] = textCoord.y();
			data[pos++] = textCoord.z();
		}
		return data;
	}
}
