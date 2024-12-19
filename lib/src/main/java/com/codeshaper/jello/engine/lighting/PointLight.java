package com.codeshaper.jello.engine.lighting;

import org.joml.Math;

import com.codeshaper.jello.editor.GizmoDrawer;
import com.codeshaper.jello.editor.property.modifier.ExposeField;
import com.codeshaper.jello.editor.property.modifier.MinValue;
import com.codeshaper.jello.engine.ComponentIcon;
import com.codeshaper.jello.engine.ComponentName;

/**
 * The Point Light component provides light in all directions from a single
 * point. Like {@link SpotLight}s, the light diminishes over distance, to 0.
 * <p>
 * Point Lights work well for anything that sends light out in all directions,
 * like light bulbs, fire and explosions.
 */
@ComponentName("Light/Point Light")
@ComponentIcon("/_editor/componentIcons/light.png")
public final class PointLight extends AbstractLight {

	@ExposeField
	@MinValue(0)
	private float range = 10f;

	@Override
	public void onDrawGizmos(GizmoDrawer gizmos, boolean isSelected) {
		super.onDrawGizmos(gizmos, isSelected);
		
		if(isSelected) {
			gizmos.color(AbstractLight.gizmoColor);
			gizmos.drawWireSphere(this.gameObject().getPosition(), this.range);
		}
	}
	
	public float getRange() {
		return this.range;
	}
	
	public void setRange(float range) {
		this.range = Math.max(0, range);
	}
}
