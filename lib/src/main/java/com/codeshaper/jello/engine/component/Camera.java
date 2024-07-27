package com.codeshaper.jello.engine.component;

import java.util.ArrayList;
import java.util.List;

import org.joml.Math;
import org.joml.Matrix4f;

import com.codeshaper.jello.editor.property.modifier.DisplayAs;
import com.codeshaper.jello.editor.property.modifier.MinValue;
import com.codeshaper.jello.editor.property.modifier.Range;
import com.codeshaper.jello.editor.property.modifier.Space;
import com.codeshaper.jello.engine.Color;
import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.Perspective;

/**
 * The Camera component is responsible for rendering your scene to the display.
 * There can be an unlimited number of Camera's at any time. The order in which
 * camera are rendered is controlled by the {@link Camera#depth} field.
 */
public class Camera extends JelloComponent {

	private static List<Camera> cameras = new ArrayList<Camera>();

	/**
	 * Gets all Cameras that exist (enabled or not) in all of the currently loaded
	 * Scenes. Cameras are added in {@link JelloComponent#onStart()} and removed in
	 * {@link JelloComponent#onDestroy()}.
	 * 
	 * @return a list of loaded Cameras.
	 */
	public static Iterable<Camera> getAllCameras() {
		return Camera.cameras;
	}

	public Color backgroundColor = new Color(0, 0, 0);
	public Perspective perspective = Perspective.PERSPECTVE;

	@Space

	/**
	 * Controls the Camera's field of view when {@link Camera#perspective} is
	 * {@link Perspective#PERSPECTVE}.
	 */
	@DisplayAs("Field of View")
	@Range(min = 0f, max = 180f)
	private float fov = 60f;
	/**
	 * Controls the Camera's zoom when {@link Camera#perspective} is
	 * {@link Perspective#ORTHOGRAPHIC}.
	 */
	@MinValue(0)
	public float zoom = 1f;
	public float nearPlane = 0.01f;
	public float farPlane = 1000f;

	@Space

	/**
	 * Controls the order in which Cameras are rendered in. Cameras with higher
	 * depths will be rendered on top of Cameras with lower depths.
	 */
	public int depth;

	private transient Matrix4f projectionMatrix;

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

	/**
	 * Gets the Camera's field of view.
	 * 
	 * @return the Camera's field of view.
	 */
	public float getFieldOfView() {
		return this.fov;
	}

	/**
	 * Sets the Camera's field of view. {@code fov} is clamped between 0 and 180.
	 * 
	 * @param fov the field of view.
	 */
	public void setFieldOfView(float fov) {
		this.fov = Math.clamp(0, 180, fov);
	}

	public void refreshProjectionMatrix(int width, int height) {
		if (this.perspective == Perspective.PERSPECTVE) {
			this.projectionMatrix.setPerspective(Math.toRadians(this.fov), (float) width / height, this.nearPlane,
					this.farPlane);
		} else {
			this.projectionMatrix.setOrtho(-this.zoom, this.zoom, -this.zoom, this.zoom, this.nearPlane, this.farPlane);
		}
	}

	public Matrix4f getProjectionMatrix() {
		return this.projectionMatrix;
	}
}
