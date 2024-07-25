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
	private List<GameObject> children;
	private Vector3f localPosition;
	private Quaternionf localRotation;
	private Vector3f localScale;
	transient Scene scene;
	private transient GameObject parent;

	/**
	 * Creates a new GameObject and adds it to a {@link Scene}.
	 * 
	 * @param name  the name of the GameObject.
	 * @param scene the {@link Scene} to add the GameObjet to.
	 * @throws IllegalArgumentException if {@code scene} is null.
	 */
	public GameObject(String name, Scene scene) {
		this(name);

		if (scene == null) {
			throw new IllegalArgumentException("scene may not be null.");
		}

		scene.moveGameObjectTo(this);
	}

	/**
	 * Creates a new GameObject and makes it a child of {@code parent}.
	 * 
	 * @param name   the name of the GameObject.
	 * @param parent the GameObject to make the parent.
	 * @throws IllegalArgumentException if {@code parent} is null.
	 */
	public GameObject(String name, GameObject parent) {
		this(name);

		if (parent == null) {
			throw new IllegalArgumentException("parent may not be null.");
		}

		Scene parentScene = parent.getScene();
		this.scene = parentScene;
		parentScene.moveGameObjectTo(this);
		this.setParent(parent);
	}

	private GameObject(String name) {
		this.setName(name);
		this.isEnabled = true;
		this.children = new ArrayList<GameObject>();
		this.components = new ArrayList<JelloComponent>();

		this.localPosition = new Vector3f(0f, 0f, 0f);
		this.localRotation = new Quaternionf();
		this.localScale = new Vector3f(1f, 1f, 1f);
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
	 * Sets the GameObject's name. Passing {@code null} will set the name to a blank
	 * String.
	 */
	public void setName(String name) {
		this.name = name != null ? name : StringUtils.EMPTY;
	}

	@Override
	public Editor<?> getInspectorDrawer() {
		return new GameObjectEditor(this);
	}

	/**
	 * Gets the Scene that this GameObject resides in.
	 * 
	 * @return the {@link Scene} this GameObject is in.
	 */
	public Scene getScene() {
		return this.scene;
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
	 * Destroys the GameObject and all of it's children.
	 */
	public void destroy() {
		if (this.isRoot()) {
			this.scene.remove(this);
		} else {
			int index = this.getIndexOf();
			this.parent.children.remove(index);
		}

		// TODO invoke destroy callbacks
	}

	/**
	 * Gets the GameObject's parent. If it has no parent, {@code null} is returned.
	 * 
	 * @return this GameObject's parent.
	 */
	public GameObject getParent() {
		return this.parent;
	}

	/**
	 * Sets the GameObject's parent. A GameObject can only be parented to another
	 * GameObject in the same Scene. Pass {@code null} to make the GameObject a root
	 * GameObject.
	 * 
	 * @param newParent the GameObject's parent, or null.
	 * 
	 * @see GameObject#scene
	 * @see Scene#moveGameObjectTo(GameObject)
	 */
	public void setParent(GameObject parent) {
		if (parent != null && this.scene != parent.scene) {
			return; // this and newParent belong to different scenes.
		}
		
		if(parent != null && parent.isDescendantOf(this)) {
			return;
		}

		if (this.parent != null) {
			// Remove the GameObject from it's previous parent.
			int index = this.getIndexOf();
			this.parent.children.remove(index);
		}

		if (parent == null) {
			this.parent = null;
			this.scene.moveGameObjectTo(this);
		} else {
			this.parent = parent;
			this.parent.children.add(this);
		}
	}

	/**
	 * Checks if this GameObject is a descendant of another GameObject (it is this
	 * GameObject, one of this GameObject's children, or a descendant of one of this
	 * GameObject's children). If <code>other</code> is null, this method returns
	 * false.
	 * <p>
	 * Note: a GameObject is considerer an descendant of itself.
	 * 
	 * @param parent
	 * @return true if this is a child of, or a child of a child, ect., of
	 *         {@code parent}
	 */
	public boolean isDescendantOf(GameObject other) {
		if (other == null) {
			return false;
		}

		return other.isAncestorOf(this);
	}

	/**
	 * Checks if a GameObject is an ancestor of this GameObject (it is this
	 * GameObject, this GameObject's parent, or an ancestor of this GameObject's
	 * parent). If <code>other</code> is null, this method returns false.
	 * <p>
	 * Note: a GameObject is considerer an ancestor of itself.
	 * 
	 * @param other GameObject to check if it's an ancestor of this.
	 * @return {@code true} if other is an ancestor of this GameObject.
	 */
	public boolean isAncestorOf(GameObject other) {
		if (other == null) {
			return false;
		}

		GameObject ancestor = this;

		do {
			if (ancestor == other) {
				return true;
			}
		} while ((ancestor = ancestor.getParent()) != null);

		return false;
	}

	/**
	 * Checks if a GameObject is siblings with {@code other}. Siblings are
	 * GameObjects that share the same immediate parent. GameObjects are also
	 * siblings if both of them are root GameObjects, meaning they have no parent.
	 * 
	 * @param other
	 * @return {@code true} if {@code other} and this GameObject are siblings.
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
	 * Gets the number of children this GameObject has.
	 * 
	 * @return the number of children.
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

	/**
	 * Gets the order in the hierarchy of this GameObject in it's parent. If this is
	 * a root GameObject, -1 is returned.
	 * 
	 * @return this GameObject's order in the hierarchy.
	 */
	public int getIndexOf() {
		GameObject parent = this.getParent();
		if (parent == this) {
			return -1; // This is a root GameObject.
		}

		int childCount = parent.getChildCount();
		for (int i = 0; i < childCount; i++) {
			if (parent.children.get(i) == this) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Adds a new {@link JelloComponent} to this GameObject and returns the new
	 * component.
	 * 
	 * @param <T>
	 * @param type
	 * @return the new component.
	 */
	public <T extends JelloComponent> T addComponent(Class<T> type) {
		// TODO handle errors.
		try {
			T component = type.getDeclaredConstructor(GameObject.class).newInstance(this);
			this.components.add(component);
			return component;
		} catch (ExceptionInInitializerError e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException | IllegalAccessException | InstantiationException | NoSuchMethodException
				| SecurityException e) { // These should never happen.
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Gets a {@link JelloComponent} of a specific type.
	 * 
	 * @param <T>
	 * @param type
	 * @return
	 */
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

	/**
	 * Gets a {@link List} of {@link JelloComponent}s of a matching type on this
	 * GameObject.
	 * 
	 * @param <T>
	 * @param type
	 * @return a {@link List} of components.
	 */
	public <T extends JelloComponent> List<T> getComponents(Class<T> type) {
		ArrayList<T> components = new ArrayList<T>();

		if (type == null) {
			return components;
		}

		for (JelloComponent component : this.components) {
			if (type.isInstance(component)) {
				components.add(type.cast(component));
			}
		}

		return components;
	}

	/**
	 * Checks if the GameObject has a {@link JelloComponent} of a specific type.
	 * 
	 * @param <T>
	 * @param type
	 * @return {@code true} if this GameObject has a component.
	 */
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
	 * @return {@code true} if the component was removed.
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
	 * Removes a {@link JelloComponent} from the GameObject. If the
	 * {@link JelloComponent} is not on this GameObject, nothing happens.
	 * 
	 * @param component the {@link JelloComponent} to remove.
	 * @return {@code true} if the component was removed.
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
	 * Gets the number of {@link JelloComponent} attached to this GameObject.
	 * 
	 * @return the number of attached components.
	 */
	public int getComponentCount() {
		return this.components.size();
	}

	/**
	 * Gets the {@link JelloComponent} at a index.
	 * 
	 * @param index
	 * @return
	 * @see GameObject#getComponentCount()
	 */
	public JelloComponent getComponentAtIndex(int index) {
		return this.components.get(index);
	}

	public Iterable<JelloComponent> getAllComponents() {
		return this.components;
	}

	/**
	 * Checks if the GameObject is enabled.
	 * 
	 * @return {@code true} if the GameObject is enabled.
	 */
	public boolean isEnabled() {
		return this.isEnabled;
	}

	/**
	 * Enables or disables the GameObject.
	 * 
	 * @param enabled should the GameObject enabled
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
}
