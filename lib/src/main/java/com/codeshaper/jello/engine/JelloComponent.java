package com.codeshaper.jello.engine;

import javax.swing.JPanel;

import org.joml.Vector3f;

import com.codeshaper.jello.editor.GizmoDrawer;
import com.codeshaper.jello.editor.inspector.ComponentEditor;
import com.codeshaper.jello.editor.inspector.Editor;
import com.codeshaper.jello.editor.property.ExposedField;
import com.codeshaper.jello.editor.property.modifier.Button;

/**
 * Base class for all Components that can be attached to GameObjects.
 * <p>
 * Component's constructs should never be invoked, instead you should use
 * {@link GameObject#addComponent(Class)} to create a new Component.
 * Initialization should be done through {@link JelloComponent#onConstruct()}
 * and @link {@link JelloComponent#onStart()}.
 * <p>
 * Components can customize their presentation in the Editor through annotations
 * and a custom {@link ComponentEditor}.
 * <p>
 * The {@link ComponentIcon} annotation specifies a custom icon to use in the
 * Editor. <br>
 * The {@link ComponentName} specifies the name of the component in the Add
 * Component popup.
 * <p>
 * {@link JelloComponent#getInspectorDrawer()} can be overridden to provide a
 * custom Component Editor. The default Component Editor simply draws all fields
 * that are either public or have the {@link ExposedField} annotations, and
 * buttons for all methods that have the {@link Button} annotation.
 */
public abstract class JelloComponent extends JelloObject {

	/**
	 * Is the component enabled, and thus should receive callbacks.
	 */
	boolean enabled;
	/**
	 * The {@link GameObject} this component is attached to.
	 */
	transient GameObject owner;
	/**
	 * Has the {@link JelloComponent#onStart()} method been invoked yet. This flag
	 * is necessary because the onStart method is not always invoked when the
	 * GameObject is created, in the event of the GameObject being instantiated from
	 * a prefab where it is disabled.
	 */
	transient boolean hasOnStartBeenCalled;

	/**
	 * Gets the {@link GameObject} that owns this Component. The owning GameObject
	 * is the GameObject that this Component is attached to.
	 * <p>
	 * This method should more accurately be nammed "getGameObject" but has been
	 * shortened to keep code lines shorter.
	 * 
	 * @return the GameObject that owns this GameObject
	 */
	public final GameObject gameObject() {
		return this.owner;
	}

	@Override
	public final String getPersistencePath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Editor<?> getEditor(JPanel panel) {
		return new ComponentEditor<JelloComponent>(this, panel);
	}

	/**
	 * TODO write this
	 * 
	 * @param enabled should the Component be enabled
	 */
	public final void setEnabled(boolean enabled) {
		if (this.enabled == enabled) {
			return; // Nothing changed.
		}

		boolean wasEnabled = this.enabled;

		this.enabled = enabled;

		if (this.gameObject().isActiveInScene()) {
			if (!wasEnabled && enabled) {
				this.invokeOnEnable();
			} else if (wasEnabled && !enabled) {
				this.invokeOnDisable();
			}
		}
	}

	/**
	 * Checks if the Component is enabled. This only checks the component itself is
	 * enabled flag and not if owning {@link GameObject} is active in it's Scene.
	 * This means that while a component may be enabled, it won't necessarily
	 * receive callbacks.
	 * 
	 * @return {@code true} if the Component is enabled.
	 * @see GameObject#isActiveInScene()
	 */
	public final boolean isEnabled() {
		return this.enabled;
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
		return this.enabled && this.owner.isActiveInScene();
	}

	/**
	 * Destroys the Component and removes it from it's owning {@link GameObject}. If
	 * this Component has already been destroyed, nothing happens. When a Component
	 * is destroyed, it is first removed from from it's owner's list of Components,
	 * then the {@link JelloComponent#onDisable()} and
	 * {@link JelloComponent#onDestroy()} callbacks are invoked. These callbacks are
	 * not invoked in the Editor.
	 */
	@Override
	public void destroy() {
		if (this.isDestroyed()) {
			return; // Already destroyed, don't do anything.
		}

		super.destroy();

		this.gameObject().removeComponent(this);

		if (Application.isPlaying()) {
			if (this.isEnabled()) {
				this.invokeOnDisable();
			}
			this.invokeOnDestroy();
		}
	}

	/**
	 * {@code onConstruct} is called when a Component is first created. This is
	 * where initialization happens that you would normally do in the constructor.
	 * Unlike {@link JelloComponent#onStart()}, this will be called even if the
	 * Component is disabled and/or it's owning GameObject is disabled.
	 * <p>
	 * Execution of this method is wrapped in a try block, so if an exception is
	 * thrown it will not compromise the state of the Application or Editor.
	 */
	protected void onConstruct() {

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
	protected void onStart() {

	}

	/**
	 * {@code onUpdate} is called every frame that the Component is enabled within
	 * the {@link Scene} Game logic should happen here.
	 * <p>
	 * Execution of this method is wrapped in a try block, so if an exception is
	 * thrown it will not compromise the state of the Application or Editor.
	 * 
	 * @param deltaTime the amount of time since the last call to onUpdate.
	 * @see JelloComponent#isEnabledInScene()
	 */
	protected void onUpdate(float deltaTime) {

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
	protected void onEnable() {

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
	protected void onDisable() {

	}

	/**
	 * Called when the component is about to be destroyed. This can be from the
	 * Component being remove from it's owner, of it's owner being destroyed.
	 * <p>
	 * Execution of this method is wrapped in a try block, so if an exception is
	 * thrown it will not compromise the state of the Application or Editor.
	 */
	protected void onDestroy() {

	}

	/**
	 * Called every frame to draw gizmos (helpful lines, icons, and more) for the
	 * Component. This is not called if gizmos are disabled in the Scene view, and
	 * never in builds.
	 * <p>
	 * By default this method will draw handles showing the orientation of the
	 * owning GameObject. It is recommended to still call this when overriding this
	 * method, but not necessary.
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
			Vector3f position = this.owner.getPosition();
			Vector3f preAllocVec = new Vector3f();
			gizmos.color(Color.red);
			gizmos.drawRay(position, this.owner.getRight(preAllocVec));
			gizmos.color(Color.green);
			gizmos.drawRay(position, this.owner.getUp(preAllocVec));
			gizmos.color(Color.blue);
			gizmos.drawRay(position, this.owner.getForward(preAllocVec));
		}
	}

	// Called internally by the Editor.
	final void invokeOnConstruct() {
		// When Play Mode is exited and started again, this flag is never reset. Hacky
		// temp fix right here...
		this.hasOnStartBeenCalled = false;

		try {
			this.onConstruct();
		} catch (Exception e) {
			Debug.log(e, this);
		}
	}

	// Called internally by the Editor.
	final void invokeOnStart() {
		try {
			this.onStart();
		} catch (Exception e) {
			Debug.log(e, this);
		}
	}

	// Called internally by the Editor.
	final void invokeOnEnable() {
		try {
			this.onEnable();
		} catch (Exception e) {
			Debug.log(e, this);
		}
	}

	// Called internally by the Editor.
	final void invokeOnUpdate(float deltaTime) {
		try {
			this.onUpdate(deltaTime);
		} catch (Exception e) {
			Debug.log(e, this);
		}
	}

	// Called internally by the Editor.
	final void invokeOnDisable() {
		try {
			this.onDisable();
		} catch (Exception e) {
			Debug.log(e, this);
		}
	}

	// Called internally by the Editor.
	final void invokeOnDestroy() {
		try {
			this.onDestroy();
		} catch (Exception e) {
			Debug.log(e, this);
		}
	}
}