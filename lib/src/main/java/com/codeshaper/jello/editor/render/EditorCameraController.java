package com.codeshaper.jello.editor.render;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.codeshaper.jello.editor.EditorProperties;
import com.codeshaper.jello.editor.JelloEditor;

/**
 * Provides controls for moving the scene view camera.
 * 
 * Controls: Scroll Wheel: Zooms in and out. MMB + Move Cursor: Pan camera. RMB
 * + Move Cursor: Rotate camera.
 */
public class EditorCameraController implements MouseListener, MouseMotionListener, MouseWheelListener {

	private static final float ZOOM_SPEED = 1f;
	private static final float ROTATE_SPEED = 0.01f;
	private static final float PAN_SPEED = 0.02f;

	private Vector3f position;
	private float xRot = 0f;
	private float yRot = 0f;
	private Matrix4f viewMatrix;

	/**
	 * Is the middle mouse button pressed?
	 */
	private boolean isMMBDown = false;
	/**
	 * Is the right mouse button pressed?
	 */
	private boolean isRMBDown = false;
	private Point pointLastPos;

	public EditorCameraController() {
		EditorProperties props = JelloEditor.instance.properties;

		this.position = new Vector3f(
				props.getFloat("camera.position.x", 0),
				props.getFloat("camera.position.y", 0),
				props.getFloat("camera.position.z", 0));
		this.xRot = props.getFloat("camera.rotation.x", 0);
		this.yRot = props.getFloat("camera.rotation.y", 0);
		
		this.viewMatrix = new Matrix4f();
		
		this.recalculate();
		
		JelloEditor.instance.addProjectSaveListener(() -> this.saveToProperties());
	}

	/**
	 * Gets the Camera's view matrix.
	 * 
	 * @return the Camera's view matrix.
	 */
	public Matrix4f getViewMatrix() {
		return this.viewMatrix;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		float scroll = e.getWheelRotation();
		Vector3f direction = new Vector3f();
		this.viewMatrix.positiveZ(direction).mul(scroll * ZOOM_SPEED);
		this.position.add(direction);
		this.recalculate();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		Point current = e.getPoint();
		Point motion = new Point(current.x - this.pointLastPos.x, current.y - this.pointLastPos.y);

		if (this.isMMBDown) {
			// Pan.
			Vector3f vec = new Vector3f();
			
			// Up/down.
			this.viewMatrix.positiveY(vec).mul(motion.y * PAN_SPEED);
			this.position.add(vec);

			// Left/right.
			this.viewMatrix.positiveX(vec).mul((motion.x * -1) * PAN_SPEED);
			this.position.add(vec);
		}

		if (this.isRMBDown) {
			// Rotate.
			this.xRot += motion.y * -1f * ROTATE_SPEED;
			this.yRot += motion.x * -1f * ROTATE_SPEED;
		}

		this.recalculate();

		this.pointLastPos = current;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		this.pointLastPos = e.getPoint();

		if (e.getButton() == MouseEvent.BUTTON3) {
			this.isRMBDown = true;
		}

		if (e.getButton() == MouseEvent.BUTTON2) {
			this.isMMBDown = true;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			this.isRMBDown = false;
		}

		if (e.getButton() == MouseEvent.BUTTON2) {
			this.isMMBDown = false;
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * Recalculates the controllers view matrix. This should be called whenever the
	 * {@link CameraController#position}, {@link CameraController#xRot}, or
	 * {@link CameraController#yRot} is changed.
	 */
	private void recalculate() {
		this.viewMatrix.identity().rotateX(this.xRot).rotateY(this.yRot);
		this.viewMatrix.translate(-this.position.x, -this.position.y, -this.position.z);
	}
	
	private void saveToProperties() {
		EditorProperties props = JelloEditor.instance.properties;
		props.setFloat("camera.position.x", this.position.x);
		props.setFloat("camera.position.y", this.position.y);
		props.setFloat("camera.position.z", this.position.z);
		props.setFloat("camera.rotation.x", this.xRot);
		props.setFloat("camera.rotation.y", this.yRot);
	}
}