package com.codeshaper.jello.engine.rendering;

import java.util.ArrayList;
import java.util.List;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import com.codeshaper.jello.editor.GizmoDrawer;
import com.codeshaper.jello.editor.property.modifier.DisplayAs;
import com.codeshaper.jello.editor.property.modifier.ExposeField;
import com.codeshaper.jello.editor.property.modifier.MinValue;
import com.codeshaper.jello.editor.property.modifier.Range;
import com.codeshaper.jello.editor.property.modifier.Space;
import com.codeshaper.jello.engine.Color;
import com.codeshaper.jello.engine.ComponentIcon;
import com.codeshaper.jello.engine.ComponentName;
import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.JelloComponent;

/**
 * The Camera component is responsible for rendering your scene to the display.
 * There can be an unlimited number of Camera's at any time. The order in which
 * camera are rendered is controlled by the {@link Camera#depth} field.
 */
@ComponentName("Rendering/Camera")
@ComponentIcon("/editor/componentIcons/camera.png")
public final class Camera extends JelloComponent {

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
	@ExposeField
	@DisplayAs("Field of View")
	@Range(min = 0f, max = 180f)
	private float fov = 60f;
	/**
	 * Controls the Camera's zoom when {@link Camera#perspective} is
	 * {@link Perspective#ORTHOGRAPHIC}.
	 */
	@MinValue(0)
	public float zoom = 1f;
	@ExposeField
	@MinValue(0)
	private float nearPlane = 0.01f;
	@ExposeField
	@MinValue(0)
	private float farPlane = 1000f;

	public Vector2f viewportPosition = new Vector2f(0, 0);
	public Vector2f viewportSize = new Vector2f(1, 1);

	@Space

	/**
	 * Controls the order in which Cameras are rendered in. Cameras with higher
	 * depths will be rendered on top of Cameras with lower depths.
	 */
	public int depth;
	
	@Space
	
	public Color fogColor = Color.gray;
	@MinValue(0)
	public float fogDensity = 0;

	private transient Matrix4f projectionMatrix = new Matrix4f();

	@Override
	protected void onStart() {
		super.onStart();

		Camera.cameras.add(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		Camera.cameras.remove(this);
	}

	@Override
	public void onDrawGizmos(GizmoDrawer gizmos, boolean isSelected) {
		super.onDrawGizmos(gizmos, isSelected);

		float aspect = 1f;
		GameObject gameObject = this.getOwner();

		gizmos.color(Color.white);
		gizmos.drawFrustum(
				gameObject.getPosition(),
				gameObject.getRotation(),
				this.fov,
				this.nearPlane,
				this.farPlane,
				aspect);
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

	public float getNearPlane() {
		return this.nearPlane;
	}

	public void setNearPlane(float distance) {
		this.nearPlane = Math.max(0, distance);
	}

	public float getFarPlane() {
		return this.farPlane;
	}

	public void setFarPlane(float distance) {
		this.farPlane = Math.max(0, distance);
	}

	/**
	 * @param width  the width in pixels.
	 * @param height the heght in pixels.
	 */
	public void refreshProjectionMatrix(float width, float height) {
		if (this.perspective == Perspective.PERSPECTVE) {
			this.projectionMatrix.setPerspective(Math.toRadians(this.fov), width / height, this.nearPlane,
					this.farPlane);
		} else {
			this.projectionMatrix.setOrtho(-this.zoom, this.zoom, -this.zoom, this.zoom, this.nearPlane, this.farPlane);
		}
	}

	/**
	 * The projection matrix of the Camera. This is null in the Editor, and only set
	 * during play mode.
	 * 
	 * @return the Camera's project matrix.
	 */
	public Matrix4f getProjectionMatrix() {
		return this.projectionMatrix;
	}
}
