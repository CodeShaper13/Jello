package com.codeshaper.jello.engine.component;

import java.util.ArrayList;
import java.util.List;

import org.joml.Math;
import org.joml.Matrix4f;

import com.codeshaper.jello.editor.property.modifier.MinValue;
import com.codeshaper.jello.editor.property.modifier.Range;
import com.codeshaper.jello.engine.Color;
import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.Perspective;

public class Camera extends JelloComponent {

	private static List<Camera> cameras = new ArrayList<Camera>();

	public static Iterable<Camera> getAllCameras() {
		return Camera.cameras;
	}

	public Color backgroundColor = Color.blue;
	public Perspective perspective = Perspective.PERSPECTVE;
	@Range(min = 0f, max = 180f)
	public float fov = 60f;
	@MinValue(0)
	public float zoom = 1f;
	public float nearPlane = 0.01f;
	public float farPlane = 1000f;

	private Matrix4f projectionMatrix;

	public Camera(GameObject owner) {
		super(owner);

		this.projectionMatrix = new Matrix4f();
		this.refreshProjectionMatrix(100, 100);
	}

	@Override
	public void onStart() {
		super.onStart();

		Camera.cameras.add(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		Camera.cameras.remove(this);
	}

	public void refreshProjectionMatrix(int width, int height) {
		if (this.perspective == Perspective.PERSPECTVE) {
			this.projectionMatrix.setPerspective(Math.toRadians(this.fov), (float) width / height, this.nearPlane,
					this.farPlane);
		} else { // Orthographic
			this.projectionMatrix.setOrtho(-this.zoom, this.zoom, -this.zoom, this.zoom, this.nearPlane, this.farPlane);
		}
	}

	public Matrix4f getProjectionMatrix() {
		return this.projectionMatrix;
	}
}
