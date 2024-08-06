package com.codeshaper.jello.engine;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class MathHelper {

	private MathHelper() {
	}

	public static Vector3f radiansToDegrees(Vector3f radians) {
		return radians.set(
				(float) Math.toDegrees(radians.x),
				(float) Math.toDegrees(radians.y),
				(float) Math.toDegrees(radians.z));
	}

	public static Vector3f degreesToRadians(Vector3f degrees) {
		return degrees.set(
				(float) Math.toRadians(degrees.x),
				(float) Math.toRadians(degrees.y),
				(float) Math.toRadians(degrees.z));
	}

	public static Vector3f quaternionToEulerAnglesDegrees(Quaternionf q) {
		return quaternionToEulerAnglesDegreesNonAlloc(q, new Vector3f());
	}

	public static Vector3f quaternionToEulerAnglesDegreesNonAlloc(Quaternionf q, Vector3f vector) {
		Vector3f vR = q.getEulerAnglesXYZ(vector);
		return new Vector3f((float) Math.toDegrees(vR.x), (float) Math.toDegrees(vR.y), (float) Math.toDegrees(vR.z));
	}
	
	public static Quaternionf quaternionFromEulerAnglesRadians(Vector3f eulerAnglesRadians) {
		return quaternionFromEulerAnglesRadiansNonAlloc(eulerAnglesRadians, new Quaternionf());
	}

	public static Quaternionf quaternionFromEulerAnglesRadiansNonAlloc(Vector3f eulerAnglesRadians, Quaternionf quaternion) {
		return quaternion.rotationZYX(eulerAnglesRadians.z, eulerAnglesRadians.y, eulerAnglesRadians.x);
	}
	
	public static Quaternionf quaternionFromEulerAnglesDegrees(Vector3f eulerAnglesDegrees) {
		return quaternionFromEulerAnglesDegreesNonAlloc(eulerAnglesDegrees, new Quaternionf());
	}
	
	public static Quaternionf quaternionFromEulerAnglesDegreesNonAlloc(Vector3f eulerAnglesDegrees, Quaternionf quaternion) {
		eulerAnglesDegrees = MathHelper.degreesToRadians(eulerAnglesDegrees);
		return quaternion.rotationXYZ(eulerAnglesDegrees.x, eulerAnglesDegrees.y, eulerAnglesDegrees.z);
	}
}
