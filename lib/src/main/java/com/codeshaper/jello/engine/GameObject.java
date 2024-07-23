package com.codeshaper.jello.engine;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.codeshaper.jello.editor.inspector.Editor;
import com.codeshaper.jello.editor.inspector.GameObjectEditor;
import com.codeshaper.jello.editor.inspector.IInspectable;
import com.codeshaper.jello.engine.component.JelloComponent;

public class GameObject implements IInspectable {

	private String name;
	private boolean isEnabled;
	private List<JelloComponent> components;
	private transient GameObject parent;
	private List<GameObject> children;
	private Vector3f localPosition;
	private Quaternionf localRotation;
	private Vector3f localScale;

	public GameObject(String name) {
		this.name = name;
		this.isEnabled = true;
		this.children = new ArrayList<GameObject>();
		this.components = new ArrayList<JelloComponent>();

		this.localPosition = new Vector3f(0f, 0f, 0f);
		this.localRotation = new Quaternionf();
		this.localScale = new Vector3f(1f, 1f, 1f);
	}

	public GameObject() {
		this("GameObject");
	}
	
	@Override
	public String toString() {
		return this.getName();
	}

	/**
	 * Gets the GameObject's name. It will never be null.
	 * 
	 * @return
	 */
	public String getName() {
		return this.name != null ? this.name : StringUtils.EMPTY;
	}

	/**
	 * Sets the GameObject's name. Passing {@link null} will set the name to a blank
	 * String.
	 */
	public void setName(String name) {
		this.name = name != null ? name : StringUtils.EMPTY;
	}

	@Override
	public Editor<?> getInspectorDrawer() {
		return new GameObjectEditor(this);
	}

	public Vector3f getPosition() {
		return this.localPosition;
	}

	public void setPosition(float x, float y, float z) {
		this.localPosition.set(x, y, z);
	}

	public void setPosition(Vector3f position) {
		this.localPosition.set(position);
	}

	public Quaternionf getRotation() {
		return this.localRotation;
	}

	public void setRotation(Quaternionf rotation) {
		this.localRotation.set(rotation);
	}

	public Vector3f getEulerAngles() {
		return MathHelper.quaternionToEulerAnglesDegrees(this.localRotation);
	}

	public void setEulerAngles(float x, float y, float z) {
		this.localRotation.set(MathHelper.quaternionFromEulerAnglesDegrees(new Vector3f(x, y, z)));
	}

	public void setEulerAngles(Vector3f eulerAnglesDegrees) {
		this.localRotation.set(MathHelper.quaternionFromEulerAnglesDegrees(eulerAnglesDegrees));
	}

	public Vector3f getScale() {
		return this.localScale;
	}

	public void setScale(float x, float y, float z) {
		this.localScale.set(x, y, z);
	}

	public void setScale(Vector3f scale) {
		this.localScale.set(scale);
	}

	public void translate(float x, float y, float z) {
		this.localPosition.add(new Vector3f(x, y, z));
	}

	public void translate(Vector3f translation) {
		this.localPosition.add(translation);
	}

	public void rotate(float xRotation, float yRotation, float zRotation) {
		this.localRotation.rotateXYZ((float) Math.toDegrees(xRotation), (float) Math.toDegrees(yRotation),
				(float) Math.toDegrees(zRotation));
	}

	public void rotate(float angle, Vector3f axis) {
		this.localRotation.rotateAxis(angle, axis);
	}

	public void scale(float scale) {
		this.localScale.mul(scale);
	}

	public void scale(float x, float y, float z) {
		this.localScale.mul(x, y, z);
	}

	public void scale(Vector3f scale) {
		this.localScale.mul(scale);
	}

	/**
	 * Gets the parent of this GameObject. If it has no parent, {@code null} is
	 * returned.
	 * 
	 * @return this GameObject's parent.
	 */
	public GameObject getParent() {
		return this.parent;
	}

	public void setParent(GameObject parent) {
		if (this.parent == null) {
			// TODO no parent
		}

		// TODO
	}

	/**
	 * Checks if this GameObject is a descendant of another GameObject.
	 * 
	 * @param parent
	 * @return true if this is a child of {@code parent}
	 */
	public boolean isDescendantOf(GameObject parent) {
		if (parent == null) {
			return false; // Passing false is not allowed.
		}

		if (parent == this) {
			return false; // You can not check is a GameObject is a child of itself.
		}

		if (this.parent == null) {
			return false; // This object has no parent, it is guaranteed to not be a child of the passed
							// GameObject.
		}

		if (this.parent == parent) {
			return true;
		} else {
			this.parent.isDescendantOf(parent);
		}

		return false;
	}

	/**
	 * 
	 * @param other
	 * @return true if the o
	 */
	public boolean isAncestorOf(GameObject other) {
		return false;
	}

	/**
	 * Checks if a GameObject is siblings with this GameObject. Siblings are
	 * GameObjects that share the same immediate parent. GameObjects are also
	 * siblings if both of them are root GameObjects, meaning they have no parent.
	 * 
	 * @param other
	 * @return true if {@code other} and this GameObject are siblings.
	 */
	public boolean isSiblingOf(GameObject other) {
		if (other == null) {
			return false;
		}

		return this.parent == other.parent;
	}

	/**
	 * Checks if this GameObject is a root GameObject. Root GameObjects have no
	 * parents.
	 * 
	 * @return true if this is a root GameObject.
	 */
	public boolean isRoot() {
		return this.parent == null;
	}

	/**
	 * Returns the number of children this GameObject has.
	 */
	public int getChildCount() {
		return this.children.size();
	}

	public Iterable<GameObject> getChildren() {
		return this.children;
	}

	/**
	 * Gets a child at the specified index.
	 * 
	 * @param index
	 * @return the child.
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *                                   ({@code index < 0 || index >= getChildCount()})
	 */
	public GameObject getChild(int index) {
		return this.children.get(index);
	}

	public <T extends JelloComponent> T addComponent(Class<T> type) {
		// TODO handle errors.
		try {
			T component = type.getDeclaredConstructor(GameObject.class).newInstance(this);
			this.components.add(component);
			return component;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}

		return null;
	}

	public <T extends JelloComponent> T getComponent(Class<T> type) {
		if (type == null) {
			return null;
		}

		for (JelloComponent component : this.components) {
			if (type.isInstance(component)) {
				return type.cast(component);
			}
		}
		return null;
	}

	public <T extends JelloComponent> List<T> getComponents(Class<T> type) {
		if (type == null) {
			return null;
		}

		ArrayList<T> components = new ArrayList<T>();

		for (JelloComponent component : this.components) {
			if (type.isInstance(component)) {
				components.add(type.cast(component));
			}
		}

		return components;
	}

	public <T extends JelloComponent> boolean hasComponent(Class<T> type) {
		for (JelloComponent component : this.components) {
			if (type.isInstance(component)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 
	 * @param <T>
	 * @param type
	 * @return true if a component was removed, false if one was not.
	 */
	public <T extends JelloComponent> boolean removeComponent(Class<T> type) {
		for (int i = 0; i < this.components.size(); i++) {
			JelloComponent component = this.components.get(i);
			if (type.isInstance(component)) {
				component.onDestroy();
				this.components.remove(i);
				return true;
			}
		}

		return false;
	}

	/**
	 * Gets the number of components attached to the GameObject.
	 * 
	 * @return the number of attached components.
	 */
	public int getComponentCount() {
		return this.components.size();
	}

	public JelloComponent getComponentAtIndex(int index) {
		return this.components.get(index);
	}

	// TODO protect
	public List<JelloComponent> getAllComponents() {
		return this.components;
	}

	/**
	 * 
	 * @param component
	 * @return true if the component was removed, false if it was not on this
	 *         GameObject.
	 */
	public boolean removeComponent(JelloComponent component) {
		int index = this.components.indexOf(component);
		if (index == -1) {
			return false;
		}

		component.onDisable();
		component.onDestroy();
		this.components.remove(index);
		return true;
	}

	/**
	 * Enables or disables the GameObject.
	 * 
	 * @param enabled Is the GameObject enabled
	 */
	public void setEnabled(boolean enabled) {
		if (this.isEnabled == enabled) {
			return; // Nothing changed.
		}

		this.isEnabled = enabled;

		for (int i = this.components.size() - 1; i >= 0; i--) {
			JelloComponent component = this.components.get(i);
			if (enabled) {
				component.onEnable();
			} else {
				component.onDisable();
			}
		}
	}

	public boolean isEnabled() {
		return this.isEnabled;
	}
}
