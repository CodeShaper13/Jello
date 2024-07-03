package com.codeshaper.jello.engine;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class MathHelper {

	private MathHelper() {
	}

	public static Vector3f radiansToDegrees(Vector3f radians) {
		return radians.set((float) Math.toDegrees(radians.x), (float) Math.toDegrees(radians.y),
				(float) Math.toDegrees(radians.z));
	}

	public static Vector3f degreesToRadians(Vector3f degrees) {
		return degrees.set((float) Math.toRadians(degrees.x), (float) Math.toRadians(degrees.y),
				(float) Math.toRadians(degrees.z));
	}

	public static Vector3f quaternionToEulerAnglesDegrees(Quaternionf q) {
		Vector3f vR = q.getEulerAnglesXYZ(new Vector3f());
		return new Vector3f((float) Math.toDegrees(vR.x), (float) Math.toDegrees(vR.y), (float) Math.toDegrees(vR.z));
	}

	public static Quaternionf quaternionFromEulerAnglesRadians(Vector3f eulerAnglesRadians) {
		return new Quaternionf().rotationZYX(eulerAnglesRadians.z, eulerAnglesRadians.y, eulerAnglesRadians.x);
	}
	
	public static Quaternionf quaternionFromEulerAnglesDegrees(Vector3f eulerAnglesDegrees) {
		eulerAnglesDegrees = MathHelper.degreesToRadians(eulerAnglesDegrees);
		return new Quaternionf().rotationXYZ(eulerAnglesDegrees.x, eulerAnglesDegrees.y, eulerAnglesDegrees.z);
	}
}
