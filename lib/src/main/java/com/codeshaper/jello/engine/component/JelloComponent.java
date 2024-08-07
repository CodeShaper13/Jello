package com.codeshaper.jello.engine.component;

import org.joml.Vector3f;

import com.codeshaper.jello.editor.GizmoDrawer;
import com.codeshaper.jello.editor.inspector.ComponentDrawer;
import com.codeshaper.jello.editor.property.modifier.Button;
import com.codeshaper.jello.engine.Color;
import com.codeshaper.jello.engine.GameObject;

public class JelloComponent {

	public transient GameObject gameObject;

	private boolean isEnabled;

	public JelloComponent() {

	}

	public JelloComponent(GameObject owner) {
		this.gameObject = owner;
		this.isEnabled = true;
	}

	public void setEnabled(boolean enabled) {
		if (this.isEnabled == enabled) {
			return; // Nothing changed.
		}

		this.isEnabled = enabled;
		if (enabled) {
			this.onEnable();
		} else {
			this.onDisable();
		}
	}

	public boolean isEnabled() {
		return this.isEnabled;
	}

	public void onStart() {

	}

	/**
	 * Called every frame.
	 */
	public void onUpdate(float deltaTime) {

	}

	public void onFixedUpdate() {

	}

	/**
	 * Called when component is enabled. This can be from enabling the component
	 * directly, or it's owning object becoming enabled.
	 */
	public void onEnable() {

	}

	/**
	 * Called when the component is disabled. This can be from disabling the
	 * component directly, or it's owning object becoming disabled. This will also
	 * be called when the component is removed in
	 * {@link GameObject#removeComponent(Class)} or
	 * {@link GameObject#removeComponent(JelloComponent)} before
	 * {@link JelloComponent#onDestroy()} is called.
	 */
	public void onDisable() {

	}

	/**
	 * Called when the component is removed from it's owning object.
	 */
	public void onDestroy() {

	}

	/**
	 * Called every frame to draw gizmos (helpful lines, icons, and more) for the
	 * component. This is not called if gizmos are disabled in the sceen view, and
	 * in builds.
	 * 
	 * @param gizmos     a reference to {@link GizmoDrawer} for drawing gizmos.
	 * @param isSelected {@code true} if {@link GameObject} owning this component is
	 *                   selected.
	 */
	public void onDrawGizmos(GizmoDrawer gizmos, boolean isSelected) {
		if (isSelected) {
			Vector3f position = this.gameObject.getPosition();
			Vector3f preAllocVec = new Vector3f();
			gizmos.color(Color.red);
			gizmos.drawRay(position, this.gameObject.getRight(preAllocVec));
			gizmos.color(Color.green);
			gizmos.drawRay(position, this.gameObject.getUp(preAllocVec));
			gizmos.color(Color.blue);
			gizmos.drawRay(position, this.gameObject.getForward(preAllocVec));
		}
	}

	public ComponentDrawer<?> getComponentDrawer() {
		return new ComponentDrawer<JelloComponent>(this);
	}
}