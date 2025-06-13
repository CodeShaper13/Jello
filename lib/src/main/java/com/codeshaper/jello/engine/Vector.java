package com.codeshaper.jello.engine;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * A collection of static methods for getting common Vectors. These methods are
 * not meant for high performance code, as a new Vector object is allocated from
 * every call.
 */
public final class Vector {

	private Vector() {
	}

	/**
	 * Creates a new Vector2f with the values (0, 0).
	 * <p>
	 * Warning, this is not meant for performance situations, as new Vector is
	 * allocated from every call.
	 * 
	 * @return the newly allocated Vector2f
	 */
	public static Vector2f vector2Zero() {
		return new Vector2f(0, 0);
	}

	/**
	 * Creates a new Vector2f with the values (1, 1).
	 * <p>
	 * Warning, this is not meant for performance situations, as new Vector is
	 * allocated from every call.
	 * 
	 * @return the newly allocated Vector2f
	 */
	public static Vector2f vector2One() {
		return new Vector2f(1, 1);
	}

	/**
	 * Creates a new Vector2f with the values (1, 0).
	 * <p>
	 * Warning, this is not meant for performance situations, as new Vector is
	 * allocated from every call.
	 * 
	 * @return the newly allocated Vector2f
	 */
	public static Vector2f vector2Right() {
		return new Vector2f(1, 0);
	}

	/**
	 * Creates a new Vector2f with the values (-1, 0).
	 * <p>
	 * Warning, this is not meant for performance situations, as new Vector is
	 * allocated from every call.
	 * 
	 * @return the newly allocated Vector2f
	 */
	public static Vector2f vector2fLeft() {
		return new Vector2f(-1, 0);
	}

	/**
	 * Creates a new Vector2f with the values (0, 1).
	 * <p>
	 * Warning, this is not meant for performance situations, as new Vector is
	 * allocated from every call.
	 * 
	 * @return the newly allocated Vector2f
	 */
	public static Vector2f vector2Up() {
		return new Vector2f(0, 1);
	}

	/**
	 * Creates a new Vector2f with the values (0, -1).
	 * <p>
	 * Warning, this is not meant for performance situations, as new Vector is
	 * allocated from every call.
	 * 
	 * @return the newly allocated Vector2f
	 */
	public static Vector2f vector2Down() {
		return new Vector2f(0, -1);
	}

	/**
	 * Creates a new Vector3f with the values (0, 0, 0).
	 * <p>
	 * Warning, this is not meant for performance situations, as new Vector is
	 * allocated from every call.
	 * 
	 * @return the newly allocated Vector3f
	 */
	public static Vector3f vector3Zero() {
		return new Vector3f(0, 0, 0);
	}

	/**
	 * Creates a new Vector3f with the values (1, 1, 1).
	 * <p>
	 * Warning, this is not meant for performance situations, as new Vector is
	 * allocated from every call.
	 * 
	 * @return the newly allocated Vector3f
	 */
	public static Vector3f vector3One() {
		return new Vector3f(1, 1, 1);
	}

	/**
	 * Creates a new Vector3f with the values (1, 0, 0).
	 * <p>
	 * Warning, this is not meant for performance situations, as new Vector is
	 * allocated from every call.
	 * 
	 * @return the newly allocated Vector3f
	 */
	public static Vector3f vector3Right() {
		return new Vector3f(1, 0, 0);
	}

	/**
	 * Creates a new Vector3f with the values (-1, 0, 0).
	 * <p>
	 * Warning, this is not meant for performance situations, as new Vector is
	 * allocated from every call.
	 * 
	 * @return the newly allocated Vector3f
	 */
	public static Vector3f vector3Left() {
		return new Vector3f(-1, 0, 0);
	}

	/**
	 * Creates a new Vector3f with the values (0, 1, 0).
	 * <p>
	 * Warning, this is not meant for performance situations, as new Vector is
	 * allocated from every call.
	 * 
	 * @return the newly allocated Vector3f
	 */
	public static Vector3f vector3Up() {
		return new Vector3f(0, 1, 0);
	}

	/**
	 * Creates a new Vector3f with the values (0, -1, 0).
	 * <p>
	 * Warning, this is not meant for performance situations, as new Vector is
	 * allocated from every call.
	 * 
	 * @return the newly allocated Vector3f
	 */
	public static Vector3f vector3Down() {
		return new Vector3f(0, -1, 0);
	}

	/**
	 * Creates a new Vector3f with the values (0, 0, 1).
	 * <p>
	 * Warning, this is not meant for performance situations, as new Vector is
	 * allocated from every call.
	 * 
	 * @return the newly allocated Vector3f
	 */
	public static Vector3f vector3Forward() {
		return new Vector3f(0, 0, 1);
	}

	/**
	 * Creates a new Vector3f with the values (0, 0, -1).
	 * <p>
	 * Warning, this is not meant for performance situations, as new Vector is
	 * allocated from every call.
	 * 
	 * @return the newly allocated Vector3f
	 */
	public static Vector3f vector3Back() {
		return new Vector3f(0, 0, -1);
	}

	/**
	 * Creates a new Vector4f with the values (1, 1, 1, 1).
	 * <p>
	 * Warning, this is not meant for performance situations, as new Vector is
	 * allocated from every call.
	 * 
	 * @return the newly allocated Vector4
	 */
	public static Vector4f vector4Zero() {
		return new Vector4f(0, 0, 0, 0);
	}

	/**
	 * Creates a new Vector4f with the values (1, 1, 1, 1).
	 * <p>
	 * Warning, this is not meant for performance situations, as new Vector is
	 * allocated from every call.
	 * 
	 * @return the newly allocated Vector4
	 */
	public static Vector4f vector4One() {
		return new Vector4f(1, 1, 1, 1);
	}
}
