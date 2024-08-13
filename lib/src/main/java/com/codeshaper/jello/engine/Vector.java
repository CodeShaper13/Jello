package com.codeshaper.jello.engine;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * A collection of static, helper methods for getting common Vectors. These
 * methods are not meant for high performance code, as a new vector object is
 * allocated from every call.
 */
public final class Vector {

	private Vector() { }

	public static Vector2f vector2Zero() {
		return new Vector2f(0, 0);
	}

	public static Vector2f vector2One() {
		return new Vector2f(1, 1);
	}

	public static Vector2f vector2Right() {
		return new Vector2f(1, 0);
	}

	public static Vector2f vector2fLeft() {
		return new Vector2f(-1, 0);
	}

	public static Vector2f vector2Up() {
		return new Vector2f(0, 1);
	}

	public static Vector2f vector2Down() {
		return new Vector2f(0, -1);
	}

	public static Vector3f vector3Zero() {
		return new Vector3f(0, 0, 0);
	}

	public static Vector3f vector3One() {
		return new Vector3f(1, 1, 1);
	}

	public static Vector3f vector3Right() {
		return new Vector3f(1, 0, 0);
	}

	public static Vector3f vector3Left() {
		return new Vector3f(-1, 0, 0);
	}

	public static Vector3f vector3Up() {
		return new Vector3f(0, 1, 0);
	}

	public static Vector3f vector3Down() {
		return new Vector3f(0, -1, 0);
	}

	public static Vector3f vector3Forward() {
		return new Vector3f(0, 0, 1);
	}

	public static Vector3f vector3Back() {
		return new Vector3f(0, 0, -1);
	}

	public static Vector4f vector4Zero() {
		return new Vector4f(0, 0, 0, 0);
	}

	public static Vector4f vector4One() {
		return new Vector4f(1, 1, 1, 1);
	}
}
