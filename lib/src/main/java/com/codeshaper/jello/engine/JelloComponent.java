package com.codeshaper.jello.engine;

import org.joml.Vector3f;

import com.codeshaper.jello.editor.GizmoDrawer;
import com.codeshaper.jello.editor.inspector.ComponentDrawer;
import com.codeshaper.jello.editor.property.ExposedField;
import com.codeshaper.jello.editor.property.modifier.Button;

/**
 * Base class for all Components that can be attached to GameObjects.
 * <p>
 * Component's constructs should never be invoked, instead you should use
 * {@link GameObject#addComponent(Class)} to create a new Component.
 * Initialization should be done through {@link JelloComponent#onConstruct()}
 * and @link {@link JelloComponent#onStart()}
 * <p>
 * Components can customize their presentation in the Editor through annotations
 * and a custom {@link ComponentDrawer}.
 * <p>
 * The {@link ComponentIcon} annotation specifies a custom icon to use in the
 * Editor. <br>
 * The {@link ComponentName} specifies the name of the component in the Add
 * Component popup.
 * <p>
 * {@link JelloComponent#getComponentDrawer()} can be overridden to provide a
 * custom Component Drawer. The default Component Drawer simply draws all fields
 * that are either public or have the {@link ExposedField} annotations, and
 * buttons for all methods that have the {@link Button} annotation.
 */
public abstract class JelloComponent {

	boolean isEnabled;

	transient GameObject gameObject;

	/**
	 * Gets the {@link GameObject} that owns this Component. The owning GameObject
	 * is the GameObject that this Component is attached to.
	 * 
	 * @return the GameObject that owns this GameObject.
	 */
	public final GameObject getOwner() {
		return this.gameObject;
	}

	public final void setEnabled(boolean enabled) {
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

	/**
	 * Checks if the Component is enabled. This only checks the {@code enabled} flag
	 * and not if it is active in it's Scene.
	 * 
	 * @return {@code true} if the Component is enabled.
	 * @see GameObject#isActiveInScene()
	 */
	public final boolean isEnabled() {
		return this.isEnabled;
	}

	/**
	 * Checks if the Component is enabled in it's Scene. When a Component is enabled
	 * in it's Scene, it means that both it's {@code enabled} flag is {@code true}
	 * and it's owner is active in it's Scene.
	 * 
	 * @return {@code true} if the Component is enabled in it's Scene.
	 * @see JelloComponent#isEnabled()
	 * @see GameObject#isActiveInScene()
	 */
	public final boolean isEnabledInScene() {
		return this.isEnabled && this.gameObject.isActiveInScene();
	}

	/**
	 * {@code onConstruct} is called when a Component is first created. This is
	 * where initialization happens that you would normally do in the constructor.
	 * Unlike {@link JelloComponent#onStart()}, this will be called even if the
	 * Component is disabled.
	 * <p>
	 * Execution of this method is wrapped in a try block, so if an exception is
	 * thrown it will not compromise the state of the Application or Editor.
	 */
	public void onConstruct() {

	}

	/**
	 * {@code onStart} is called on the first frame the Component is enabled within
	 * the Scene. This is where initialization happens that depends on other
	 * Components.
	 * <p>
	 * Execution of this method is wrapped in a try block, so if an exception is
	 * thrown it will not compromise the state of the Application or Editor.
	 * 
	 * @see JelloComponent#isEnabledInScene()
	 */
	public void onStart() {

	}

	/**
	 * {@code onUpdate} is called every frame. Game logic should happen here.
	 * <p>
	 * Execution of this method is wrapped in a try block, so if an exception is
	 * thrown it will not compromise the state of the Application or Editor.
	 * 
	 * @param deltaTime the amount of time since the last call to onUpdate.
	 */
	public void onUpdate(float deltaTime) {

	}

	/**
	 * Called when Component becomes enabled. A Component becomes enabled with it's
	 * {@code enabled} flag is {@code true} and it's owner is active in the scene.
	 * <p>
	 * Execution of this method is wrapped in a try block, so if an exception is
	 * thrown it will not compromise the state of the Application or Editor.
	 * 
	 * @see GameObject#isActiveInScene()
	 */
	public void onEnable() {

	}

	/**
	 * Called when the component becomes disabled. This will also be called when the
	 * component is removed in {@link GameObject#removeComponent(Class)} or
	 * {@link GameObject#removeComponent(JelloComponent)} before
	 * {@link JelloComponent#onDestroy()} is called.
	 * <p>
	 * Execution of this method is wrapped in a try block, so if an exception is
	 * thrown it will not compromise the state of the Application or Editor.
	 */
	public void onDisable() {

	}

	/**
	 * Called when the component is about to be destroyed. This can be from the
	 * Component being remove from it's owner, of it's owner being destroyed.
	 * <p>
	 * Execution of this method is wrapped in a try block, so if an exception is
	 * thrown it will not compromise the state of the Application or Editor.
	 */
	public void onDestroy() {

	}

	/**
	 * Called every frame to draw gizmos (helpful lines, icons, and more) for the
	 * component. This is not called if gizmos are disabled in the scene view, and
	 * never in builds.
	 * <p>
	 * Execution of this method is wrapped in a try block, so if an exception is
	 * thrown it will not compromise the state of the Application or Editor.
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