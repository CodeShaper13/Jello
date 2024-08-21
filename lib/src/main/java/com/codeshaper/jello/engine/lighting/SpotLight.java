package com.codeshaper.jello.engine.lighting;

import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.codeshaper.jello.editor.GizmoDrawer;
import com.codeshaper.jello.editor.property.modifier.ExposeField;
import com.codeshaper.jello.editor.property.modifier.MaxValue;
import com.codeshaper.jello.editor.property.modifier.MinValue;
import com.codeshaper.jello.engine.ComponentIcon;
import com.codeshaper.jello.engine.ComponentName;
import com.codeshaper.jello.engine.GameObject;

/**
 * The Spot Light Component provides light coming out in a cone shape, in a
 * single direction. Like {@link PointLight}s, the light diminishes over
 * distance, to 0.
 * <p>
 * Spot lights work well for anything that emits light in a single direction,
 * like a flashlight or car head light.
 */
@ComponentName("Light/Spot Light")
@ComponentIcon("/editor/componentIcons/light.png")
public final class SpotLight extends AbstractLight {

	@ExposeField
	@MinValue(0)
	private float range = 10f;
	@ExposeField
	@MinValue(0)
	@MaxValue(360)
	private float angle = 45f;

	@Override
	public void onDrawGizmos(GizmoDrawer gizmos, boolean isSelected) {
		super.onDrawGizmos(gizmos, isSelected);
		
		if(isSelected) {
			gizmos.color(AbstractLight.gizmoColor);
			
			GameObject gameObject = this.gameObject();

			Vector3f alloc0 = new Vector3f();
			Vector3f alloc1 = new Vector3f();
			Vector3f circleCenter = gameObject.getPosition().add(gameObject.getForward(alloc1).mul(this.range));
			Quaternionf circleRot = gameObject.getRotation().rotateX(Math.toRadians(90));
			
			float c = this.range / Math.cos(Math.toRadians(this.angle / 2));
			float radius = Math.sqrt((c * c) - (this.range * this.range));
						
			gizmos.drawWireCircle(circleCenter, circleRot, radius);
					
			Vector3f pos = gameObject.getPosition();
			gizmos.drawLine(pos, alloc0.set(circleCenter).add(gameObject.getUp(alloc1).mul(radius)));
			gizmos.drawLine(pos, alloc0.set(circleCenter).add(gameObject.getUp(alloc1).mul(-radius)));
			gizmos.drawLine(pos, alloc0.set(circleCenter).add(gameObject.getRight(alloc1).mul(radius)));
			gizmos.drawLine(pos, alloc0.set(circleCenter).add(gameObject.getRight(alloc1).mul(-radius)));
		}
	}

	public float getRange() {
		return this.range;
	}

	public void setRange(float range) {
		this.range = Math.max(0, range);
	}
	
	public float getAngle() {
		return this.angle;
	}
	
	public void setAngle(float angle) {
		this.angle = Math.clamp(0, 360, angle);
	}
}
