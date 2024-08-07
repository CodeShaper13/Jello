package com.codeshaper.jello.engine.component;

import org.joml.Vector3f;

import com.codeshaper.jello.editor.GizmoDrawer;
import com.codeshaper.jello.engine.ComponentIcon;
import com.codeshaper.jello.engine.GameObject;

/**
 * The Directional Light Components provides light coming from a single
 * direction on all objects. The amount of light is constant, regardless of the
 * object's distance from the directional light.
 * <p>
 * Directional Lights work well for far away light sources, like the sun or
 * moon.
 */
@ComponentIcon("/editor/componentIcons/light.png")
public class DirectionalLight extends AbstractLight {

	public DirectionalLight(GameObject owner) {
		super(owner);
	}

	@Override
	public void onDrawGizmos(GizmoDrawer gizmos, boolean isSelected) {
		super.onDrawGizmos(gizmos, isSelected);
		
		if(isSelected) {
			gizmos.color(AbstractLight.gizmoColor);
			Vector3f pos = this.gameObject.getPosition();
			gizmos.drawLine(pos, this.gameObject.getForward().mul(3).add(pos));
		}
	}
}
