package com.codeshaper.jello.engine.lighting;

import org.joml.Vector3f;

import com.codeshaper.jello.editor.GizmoDrawer;
import com.codeshaper.jello.engine.ComponentIcon;
import com.codeshaper.jello.engine.ComponentName;
import com.codeshaper.jello.engine.GameObject;

/**
 * The Directional Light Components provides light coming from a single
 * direction on all objects. The amount of light is constant, regardless of the
 * object's distance from the directional light.
 * <p>
 * Directional Lights work well for far away light sources, like the sun or
 * moon.
 */
@ComponentName("Light/Directional Light")
@ComponentIcon("/_editor/componentIcons/light.png")
public final class DirectionalLight extends AbstractLight {

	@Override
	public void onDrawGizmos(GizmoDrawer gizmos, boolean isSelected) {
		super.onDrawGizmos(gizmos, isSelected);
		
		if(isSelected) {
			gizmos.color(AbstractLight.gizmoColor);
			GameObject gameObject = this.gameObject();
			Vector3f pos = gameObject.getPosition();
			gizmos.drawLine(pos, gameObject.getForward().mul(3).add(pos));
		}
	}
}
