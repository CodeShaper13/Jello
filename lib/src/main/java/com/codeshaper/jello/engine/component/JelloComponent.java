package com.codeshaper.jello.engine.component;

import java.io.Serializable;

import com.codeshaper.jello.engine.GameObject;

public class JelloComponent implements Serializable {

	public transient GameObject gameObject;
	public boolean isEnabled;

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

	public void onRender() {

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
}