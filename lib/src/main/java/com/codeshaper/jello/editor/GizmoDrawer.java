package com.codeshaper.jello.editor;

import static org.lwjgl.opengl.GL30.*;

import org.apache.commons.lang3.NotImplementedException;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.codeshaper.jello.engine.Color;

/**
 * <p>
 * None of the passed in {@link Vector3f} and {@link Quaternionf} parameters are
 * modified in any of the methods, so it is safe to pass in values that save
 * state.
 */
public class GizmoDrawer {

	private static final int DEFAULT_CIRCLE_SEGMENTS = 24;

	// Pre-allocated objects that are used when drawing the gizmos. Should save on
	// GC collections.
	private final Vector3f p0;
	private final Vector3f p1;
	private final Vector3f p2;
	private final Vector3f p3;
	private final Vector3f p4;
	private final Vector3f p5;
	private final Vector3f p6;
	private final Vector3f p7;
	private final Quaternionf q0;
	private final Matrix4f preAllocMatrix;

	private int circleSegments;

	public GizmoDrawer() {
		this.p0 = new Vector3f();
		this.p1 = new Vector3f();
		this.p2 = new Vector3f();
		this.p3 = new Vector3f();
		this.p4 = new Vector3f();
		this.p5 = new Vector3f();
		this.p6 = new Vector3f();
		this.p7 = new Vector3f();
		this.q0 = new Quaternionf();
		this.preAllocMatrix = new Matrix4f();

		this.circleSegments = DEFAULT_CIRCLE_SEGMENTS;
	}

	/**
	 * Resets the GizmoDrawer to it's default state.
	 */
	public void reset() {
		Color defaultColor = Color.white;
		glColor3f(defaultColor.r, defaultColor.g, defaultColor.b);

		this.circleSegments = DEFAULT_CIRCLE_SEGMENTS;
	}

	/**
	 * Sets the color to draw gizmos with. If no color is explicitly set
	 * {@link Color#white} is used.
	 * 
	 * @param color the {@link Color} to draw with.
	 */
	public void color(Color color) {
		glColor3f(color.r, color.g, color.b);
	}

	/**
	 * Sets the number of line segments to use when drawing circles. Higher numbers
	 * will result in smoother circles with more overhead. The minimum number of
	 * segments is 4. Passing less than 4 for {@code segments} will set the number
	 * to 4.
	 * <p>
	 * The default number of segments is 24.
	 * 
	 * @param segments the number of line segments in circles.
	 */
	public void circleSegments(int segments) {
		if (segments <= 4) {
			segments = 4;
		}

		this.circleSegments = 24;
	}

	/**
	 * Draws a line in world space between {@code start} and {@code end}.
	 * 
	 * @param start the start of the line in world space.
	 * @param end   the end of the line in world space.
	 */
	public void drawLine(Vector3f start, Vector3f end) {
		glBegin(GL_LINES);
		glVertex3f(start.x, start.y, start.z);
		glVertex3f(end.x, end.y, end.z);
		glEnd();
	}

	/**
	 * Draws lines connecting a series of points.
	 * 
	 * @param loop   if {@code true} the first and last points are connected.
	 * @param points the points in world space to draw lines between.
	 */
	public void drawLines(boolean loop, Vector3f... points) {
		glBegin(loop ? GL_LINE_LOOP : GL_LINE_STRIP);
		Vector3f p;
		for (int i = 0; i < points.length; i++) {
			p = points[i];
			glVertex3f(p.x, p.y, p.z);
		}
		glEnd();
	}

	/**
	 * Draws a ray starting from {@code start} and going in the direction of
	 * {@code ray}
	 * 
	 * @param start     the rays start in world space.
	 * @param direction the direction of the ray in world space.
	 */
	public void drawRay(Vector3f start, Vector3f direction) {
		glBegin(GL_LINES);
		glVertex3f(start.x, start.y, start.z);
		glVertex3f(start.x + direction.x, start.y + direction.y, start.z + direction.z);
		glEnd();
	}

	/**
	 * Draws a solid cube.
	 * 
	 * @param position the position of the cube.
	 * @param size     the size of the cube.
	 * @param rotation the rotation of the cube, or {@code null} for no rotation.
	 */
	public void drawCube(Vector3f position, Quaternionf rotation, Vector3f size) {
		size.mul(0.5f); // Convert to radius.

		Matrix4f m = this.createMatrix(position, rotation);

		m.transformPosition(-size.x, -size.y, -size.z, p0);
		m.transformPosition(size.x, -size.y, -size.z, p1);
		m.transformPosition(size.x, -size.y, size.z, p2);
		m.transformPosition(-size.x, -size.y, size.z, p3);
		m.transformPosition(-size.x, size.y, -size.z, p4);
		m.transformPosition(size.x, size.y, -size.z, p5);
		m.transformPosition(size.x, size.y, size.z, p6);
		m.transformPosition(-size.x, size.y, size.z, p7);

		this.quad(p0, p3, p7, p4); // +x
		this.quad(p1, p2, p6, p5); // -x
		this.quad(p0, p1, p2, p3); // +y (top)
		this.quad(p4, p5, p6, p7); // -y (bottom)
		this.quad(p2, p3, p7, p6); // +z
		this.quad(p0, p1, p5, p4); // -z
	}

	/**
	 * Draws a wire cube.
	 * 
	 * @param position the position of the cube.
	 * @param size     the size of the cube.
	 * @param rotation the rotation of the cube, or {@code null} for no rotation.
	 */
	public void drawWireCube(Vector3f position, Quaternionf rotation, Vector3f size) {
		size.mul(0.5f); // Convert to radius.

		Matrix4f m = this.createMatrix(position, rotation);

		m.transformPosition(-size.x, -size.y, -size.z, p0);
		m.transformPosition(size.x, -size.y, -size.z, p1);
		m.transformPosition(size.x, -size.y, size.z, p2);
		m.transformPosition(-size.x, -size.y, size.z, p3);
		m.transformPosition(-size.x, size.y, -size.z, p4);
		m.transformPosition(size.x, size.y, -size.z, p5);
		m.transformPosition(size.x, size.y, size.z, p6);
		m.transformPosition(-size.x, size.y, size.z, p7);

		// Bottom
		this.drawLine(p0, p1);
		this.drawLine(p1, p2);
		this.drawLine(p2, p3);
		this.drawLine(p3, p0);

		// Top
		this.drawLine(p4, p5);
		this.drawLine(p5, p6);
		this.drawLine(p6, p7);
		this.drawLine(p7, p4);

		// Vertical lines
		this.drawLine(p0, p4);
		this.drawLine(p1, p5);
		this.drawLine(p2, p6);
		this.drawLine(p3, p7);
	}

	public void drawWireCircle(Vector3f position, Quaternionf rotation, float radius) {
		float angle = 0f;
		Matrix4f m = this.createMatrix(position, rotation);

		glBegin(GL_LINE_LOOP);

		for (int i = 0; i < this.circleSegments; i++) {
			p0.set(Math.sin(Math.toRadians(angle)) * radius, 0, Math.cos(Math.toRadians(angle)) * radius);
			m.transformPosition(p0);
			glVertex3f(p0.x, p0.y, p0.z);
			angle += (360f / this.circleSegments);
		}

		glEnd();
	}

	public void drawSphere(Vector3f position, float radius) {
		/*
		 * glPointSize(10.0f); glEnable(GL_POINT_SIZE); glEnable(GL_POINT_SMOOTH);
		 * glEnable(GL_BLEND); glBegin(GL_POINTS); glVertex3f(position.x, position.y,
		 * position.z); glEnd();
		 */
		throw new NotImplementedException(); // TODO
	}

	public void drawWireSphere(Vector3f position, float radius) {
		q0.identity();
		float radians = Math.toRadians(90);
		this.drawWireCircle(position, null, radius);
		this.drawWireCircle(position, q0.rotateZ(radians), radius);
		this.drawWireCircle(position, q0.rotateX(radians), radius);
	}

	public void drawFrustum(Vector3f position, Quaternionf rotation, float fov, float nearPlane, float farPlane,
			float aspect) {
		Matrix4f frustum = new Matrix4f().setPerspective((float) Math.toRadians(fov), aspect, nearPlane, farPlane);
		this.drawFrustum(position, rotation, frustum);
	}

	public void drawFrustum(Vector3f position, Quaternionf rotation, Matrix4f furstum) {
		Matrix4f m = this.createMatrix(position, rotation);
		Matrix4f inv = furstum.invert();

		Vector4f[] f = new Vector4f[] {
				// near face
				new Vector4f(1, 1, -1, 1), new Vector4f(-1, 1, -1, 1), new Vector4f(1, -1, -1, 1),
				new Vector4f(-1, -1, -1, 1),
				// far face
				new Vector4f(1, 1, 1, 1), new Vector4f(-1, 1, 1, 1), new Vector4f(1, -1, 1, 1),
				new Vector4f(-1, -1, 1, 1) };

		Vector3f[] v = new Vector3f[] { p0, p1, p2, p3, p4, p5, p6, p7 };
		for (int i = 0; i < 8; i++) {
			Vector4f ff = new Vector4f(f[i]).mul(inv);

			v[i].set(ff.x / ff.w, ff.y / ff.w, ff.z / ff.w);
			m.transformPosition(v[i]);
		}

		this.drawLine(v[0], v[1]);
		this.drawLine(v[0], v[2]);
		this.drawLine(v[3], v[1]);
		this.drawLine(v[3], v[2]);
		this.drawLine(v[4], v[5]);
		this.drawLine(v[4], v[6]);
		this.drawLine(v[7], v[5]);
		this.drawLine(v[7], v[6]);
		this.drawLine(v[0], v[4]);
		this.drawLine(v[1], v[5]);
		this.drawLine(v[3], v[7]);
		this.drawLine(v[2], v[6]);
	}

	public void drawIcon(Vector3f center, String path, boolean scale) {
		throw new NotImplementedException(); // TODO
	}

	private Matrix4f createMatrix(Vector3f position, Quaternionf rotation) {
		this.preAllocMatrix.identity();
		if (rotation != null) {
			this.preAllocMatrix.translationRotate(position, rotation);
		} else {
			this.preAllocMatrix.translation(position);
		}
		return this.preAllocMatrix;
	}

	private void quad(Vector3f p0, Vector3f p1, Vector3f p2, Vector3f p3) {
		glBegin(GL_QUADS);
		glVertex3f(p0.x, p0.y, p0.z);
		glVertex3f(p1.x, p1.y, p1.z);
		glVertex3f(p2.x, p2.y, p2.z);
		glVertex3f(p3.x, p3.y, p3.z);
		glEnd();
	}
}