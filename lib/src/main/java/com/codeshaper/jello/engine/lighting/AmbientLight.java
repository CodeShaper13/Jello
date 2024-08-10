package com.codeshaper.jello.engine.lighting;

import com.codeshaper.jello.editor.GizmoDrawer;
import com.codeshaper.jello.engine.ComponentIcon;
import com.codeshaper.jello.engine.GameObject;

/**
 * The Ambient Light Component provides light to every object in the scene from
 * all directions. The position and rotation of Ambient Lights has no effect.
 * <p>
 * Ambient Lights are used to provide a consitent level of lighting across the
 * scene. A white light could simulate a sterile hospital room, or an orange
 * light if your scene takes place at sunset.
 */
@ComponentIcon("/editor/componentIcons/light.png")
public final class AmbientLight extends AbstractLight {

	public AmbientLight(GameObject owner) {
		super(owner);
	}

	@Override
	public void onDrawGizmos(GizmoDrawer gizmos, boolean isSelected) {
		super.onDrawGizmos(gizmos, isSelected);
	}
}
