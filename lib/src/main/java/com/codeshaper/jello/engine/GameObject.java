package com.codeshaper.jello.engine;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.JPanel;

import org.apache.commons.lang3.StringUtils;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.inspector.Editor;
import com.codeshaper.jello.editor.inspector.GameObjectEditor;
import com.codeshaper.jello.engine.database.AssetDatabase;
import com.codeshaper.jello.engine.database.Serializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

public final class GameObject extends JelloObject {

	private String name;
	private boolean isActive;
	private List<JelloComponent> components;
	private List<GameObject> children;
	private Vector3f localPosition;
	private Quaternionf localRotation;
	private Vector3f localScale;
	private transient Matrix4f localMatrix;
	transient Scene scene;
	transient GameObject parent;

	private transient boolean isTransformDirty = true;

	/**
	 * Creates a new GameObject and adds it to a {@link Scene}.
	 * 
	 * @param name  the name of the GameObject.
	 * @param scene the {@link Scene} to add the GameObjet to.
	 * @throws IllegalArgumentException if {@code scene} is null.
	 */
	public GameObject(String name, Scene scene) {
		this();

		if (scene == null) {
			throw new IllegalArgumentException("scene may not be null.");
		}

		this.setName(name);

		scene.moveGameObjectTo(this);

		this.invokeOnConstructIfInApplication();
	}

	/**
	 * Creates a new GameObject and makes it a child of {@code parent}.
	 * 
	 * @param name   the name of the GameObject.
	 * @param parent the GameObject to make the parent.
	 * @throws IllegalArgumentException if {@code parent} is null.
	 */
	public GameObject(String name, GameObject parent) {
		this();

		if (parent == null) {
			throw new IllegalArgumentException("parent may not be null.");
		}

		this.setName(name);

		Scene parentScene = parent.getScene();
		this.scene = parentScene;
		parentScene.moveGameObjectTo(this);
		this.setParent(parent);

		this.invokeOnConstructIfInApplication();
	}

	/**
	 * Creates a {@link JsonElement} representing the GameObject.
	 * 
	 * @return a JsonElement representing the GameObject.
	 * @see GameObject#fromJson(JsonElement, GameObject)
	 * @see GameObject#fromJson(JsonElement, Scene)
	 */
	public JsonElement toJson() {
		return AssetDatabase.getInstance().serializer.serializeToJsonElement(this);
	}

	/**
	 * Creates a {@link GameObject} from Json data. If {@code json} is null or
	 * malformed, no GameObject will be created and an error will be logged.
	 * 
	 * @param json  the json to create the GameObject from
	 * @param scene the Scene to place the GameObject in
	 * @return the newly created GameObject or {@code null} on error
	 * @see GameObject#toJson()
	 */
	public static GameObject fromJson(JsonElement json, Scene scene) {
		Serializer serializer = AssetDatabase.getInstance().serializer;

		try {
			GameObject newGameObject = serializer.deserialize(json, GameObject.class);
			scene.moveGameObjectTo(newGameObject);
			scene.recursivelySetupObject(newGameObject);

			newGameObject.invokeOnConstructIfInApplication();

			return newGameObject;
		} catch (JsonSyntaxException e) {
			Debug.log(e);
			return null;
		}
	}

	/**
	 * Creates a {@link GameObject} from Json data. If {@code json} is null or
	 * malformed, no GameObject will be created and an error will be logged.
	 * 
	 * @param json   the json to create the GameObject from
	 * @param parent the GameObject to make the new GameObject a child of
	 * @return the newly created GameObject or {@code null} on error
	 */
	public static GameObject fromJson(JsonElement json, GameObject parent) {
		Serializer serializer = AssetDatabase.getInstance().serializer;
		GameObject newGameObject = serializer.deserialize(json, GameObject.class);

		Scene parentScene = parent.getScene();
		newGameObject.scene = parentScene;
		parentScene.moveGameObjectTo(newGameObject);
		newGameObject.setParent(parent);

		parentScene.recursivelySetupObject(newGameObject);

		newGameObject.invokeOnConstructIfInApplication();

		return newGameObject;
	}

	/**
	 * Locates a {@link GameObject} from it's persistent path. If no GameObject can
	 * be found at the path, {@code null} is returned.
	 * 
	 * @param path the path to the GameObject
	 * @return the GameObject as this path
	 * @see GameObject#getPersistencePath()
	 */
	public static GameObject fromPersistantPath(String path) {
		if (path == null) {
			return null;
		}

		path = path.substring(12);
		String[] strings = path.split(".jelobj", 2);
		if (strings.length == 2) {
			String sceneName = strings[0];
			Scene scene = JelloEditor.instance.sceneManager.getScene(sceneName);
			if (scene != null) {
				String gameObjPath = strings[1].substring(1);
				return scene.getGameObject(gameObjPath);
			}
		}

		return null;
	}

	private GameObject() {
		this.isActive = true;
		this.children = new ArrayList<GameObject>();
		this.components = new ArrayList<JelloComponent>();

		this.localPosition = new Vector3f(0f, 0f, 0f);
		this.localRotation = new Quaternionf();
		this.localScale = new Vector3f(1f, 1f, 1f);
		this.localMatrix = new Matrix4f();
	}



	@Override
	public String toString() {
		return this.name;
	}

	/**
	 * Gets the {@link GameObject}'s name. It will never be {@code null}.
	 * 
	 * @return
	 */
	public String getName() {
		return this.name != null ? this.name : StringUtils.EMPTY;
	}

	/**
	 * Sets the {@link GameObject}'s name. Passing {@code null} will set the name to
	 * a blank String.
	 * 
	 * @param name the {@link GameObject}'s new name
	 */
	public void setName(String name) {
		this.name = name != null ? name : StringUtils.EMPTY;
	}

	/**
	 * Gets the path in the hierarchy to the GameObject. For root GameObjects, this
	 * is just the GameObject's name from {@link GameObject#getName()}. For non-root
	 * GameObjects, the path is every parent, separated by {@code /}, then the
	 * GameObject's name. If {@code includeSceneName} is {@code true} the Scene's
	 * path will be prefixed, followed by a {@code /}
	 * 
	 * @param includeSceneName should the Scene's path be prefixed
	 * @return a path to the GameObject
	 * @see GameObject#isRoot()
	 */
	public String getPath(boolean includeSceneName) {
		if (this.getParent() != null) {
			return this.parent.getPath(includeSceneName) + "/" + this.getName();
		} else {
			return includeSceneName ? this.scene.location.getRelativePath() + "/" + this.getName() : this.getName();
		}
	}

	@Override
	public String getPersistencePath() {
		return "[GameObject]" + this.getPath(true);
	}

	@Override
	public Editor<?> getInspectorDrawer(JPanel panel) {
		return new GameObjectEditor(this, panel);
	}

	/**
	 * Gets the {@link Scene} this {@link GameObject} resides in.
	 * 
	 * @return the {@link Scene} this GameObject is in
	 */
	public Scene getScene() {
		return this.scene;
	}

	public Vector3f getPosition() {
		return this.getPosition(new Vector3f());
	}

	public Vector3f getPosition(Vector3f vector) {
		return this.getWorldMatrix().getTranslation(vector);
	}

	public Vector3f getLocalPosition() {
		return new Vector3f(this.localPosition);
	}

	public Vector3f getLocalPosition(Vector3f vector) {
		return vector.set(this.localPosition);
	}

	public void setLocalPosition(float x, float y, float z) {
		this.localPosition.set(x, y, z);
		this.isTransformDirty = true;
	}

	public void setLocalPosition(Vector3f position) {
		this.localPosition.set(position);
		this.isTransformDirty = true;
	}

	public Quaternionf getRotation() {
		return this.getRotation(new Quaternionf());
	}

	public Quaternionf getRotation(Quaternionf quaternion) {
		return this.getWorldMatrix().getNormalizedRotation(quaternion);
	}

	public Quaternionf getLocalRotation() {
		return new Quaternionf(this.localRotation);
	}

	public Quaternionf getLocalRotation(Quaternionf quaternion) {
		return quaternion.set(this.localRotation);
	}

	public void setLocalRotation(Quaternionf rotation) {
		this.localRotation.set(rotation);
		this.isTransformDirty = true;
	}

	public Vector3f getEulerAngles() {
		return MathHelper.quaternionToEulerAnglesDegrees(this.getRotation());
	}

	public Vector3f getLocalEulerAngles() {
		return MathHelper.quaternionToEulerAnglesDegrees(this.localRotation);
	}

	public void setLocalEulerAngles(float x, float y, float z) {
		this.localRotation.set(MathHelper.quaternionFromEulerAnglesDegrees(new Vector3f(x, y, z)));
		this.isTransformDirty = true;
	}

	public void setLocalEulerAngles(Vector3f eulerAnglesDegrees) {
		this.localRotation.set(MathHelper.quaternionFromEulerAnglesDegrees(eulerAnglesDegrees));
		this.isTransformDirty = true;
	}

	public Vector3f getScale() {
		return this.getScale(new Vector3f());
	}

	public Vector3f getScale(Vector3f vector) {
		return this.getWorldMatrix().getScale(vector);
	}

	public Vector3f getLocalScale() {
		return new Vector3f(this.localScale);
	}

	public Vector3f getLocalScale(Vector3f vector) {
		return vector.set(this.localScale);
	}

	public void setLocalScale(float x, float y, float z) {
		this.localScale.set(x, y, z);
		this.isTransformDirty = true;
	}

	public void setLocalScale(Vector3f scale) {
		this.localScale.set(scale);
		this.isTransformDirty = true;
	}

	public void translate(float x, float y, float z) {
		this.localPosition.add(x, y, z);
		this.isTransformDirty = true;
	}

	public void translate(Vector3f translation) {
		this.localPosition.add(translation);
		this.isTransformDirty = true;
	}

	public void rotate(float xRotation, float yRotation, float zRotation) {
		this.localRotation.rotateXYZ(
				(float) Math.toRadians(xRotation),
				(float) Math.toRadians(yRotation),
				(float) Math.toRadians(zRotation));
		this.isTransformDirty = true;
	}

	public void rotate(float angle, Vector3f axis) {
		this.localRotation.rotateAxis(angle, axis);
		this.isTransformDirty = true;
	}

	public void scale(float scale) {
		this.localScale.mul(scale);
		this.isTransformDirty = true;
	}

	public void scale(float x, float y, float z) {
		this.localScale.mul(x, y, z);
		this.isTransformDirty = true;
	}

	public void scale(Vector3f scale) {
		this.localScale.mul(scale);
		this.isTransformDirty = true;
	}

	/**
	 * Gets a normalized vector pointing to the right (along the red axis) of this
	 * GameObject. A new vector is returned so it is safe to modify. If you don't
	 * want to allocate a new vector, use {@link GameObject#getRight(Vector3f)}.
	 * 
	 * @return a vector pointing to the right
	 */
	public Vector3f getRight() {
		return this.getRight(new Vector3f());
	}

	public Vector3f getRight(Vector3f vector) {
		return this.getLocalMatrix().invert().positiveX(vector);
	}

	/**
	 * Gets a normalized vector pointing up (along the green axis) of this
	 * GameObject. A new vector is returned so it is safe to modify. If you don't
	 * want to allocate a new vector, use {@link GameObject#getUp(Vector3f)}.
	 * 
	 * @return a vector pointing up
	 */
	public Vector3f getUp() {
		return this.getUp(new Vector3f());
	}

	public Vector3f getUp(Vector3f vector) {
		return this.getLocalMatrix().invert().positiveY(vector);
	}

	/**
	 * Gets a normalized vector pointing forward (along the blue axis) of this
	 * GameObject. A new vector is returned so it is safe to modify. If you don't
	 * want to allocate a new vector, use {@link GameObject#getForward(Vector3f)}.
	 * 
	 * @return a vector pointing forward
	 */
	public Vector3f getForward() {
		return this.getForward(new Vector3f());
	}

	public Vector3f getForward(Vector3f vector) {
		return this.getLocalMatrix().invert().positiveZ(vector);
	}

	public Matrix4f getLocalMatrix() {
		return this.getLocalMatrix(new Matrix4f());
	}

	public Matrix4f getLocalMatrix(Matrix4f matrix) {
		if (this.isTransformDirty) {
			this.localMatrix.translationRotateScale(this.localPosition, this.localRotation, this.localScale);
			this.isTransformDirty = false;
		}
		return matrix.set(this.localMatrix);
	}

	public Matrix4f getWorldMatrix() {
		return this.getWorldMatrix(new Matrix4f());
	}

	public Matrix4f getWorldMatrix(Matrix4f matrix) {
		Matrix4f localMatrix = this.getLocalMatrix(matrix);
		this.func(localMatrix);
		return localMatrix;
	}

	public boolean isDirty() {
		return this.isTransformDirty;
	}

	private void func(Matrix4f m) {
		if (this.parent == null) {
			return;
		} else {
			Matrix4f parentMatrix = this.parent.getLocalMatrix();
			m.mulLocal(parentMatrix);
		}
	}

	/**
	 * Destroys the GameObject and all of it's children.
	 */
	public void destroy() {
		if (this.isDestroyed()) {
			Debug.logError("trying to destroy %s but it has already been destroyed", this.getName());
			// return; // GameObject has already been destroyed, don't do anything.
		}

		for (int i = this.getChildCount() - 1; i >= 0; i--) {
			GameObject child = this.getChild(i);
			child.destroy();
		}

		for (int i = this.getComponentCount() - 1; i >= 0; i--) {
			JelloComponent component = this.getComponentAtIndex(i);
			component.destroy();
		}

		if (this.isRoot()) {
			// Remove the GameObject from it's Scene.
			this.scene.rootGameObjects.remove(this);
		} else {
			// Remove the GameObject from it's parents child list.
			int index = this.getIndexOf();
			this.parent.children.remove(index);
		}

		super.destroy(); // Set's the isDestroyed flag
	}

	/**
	 * Gets the GameObject's parent. If it has no parent, {@code null} is returned.
	 * 
	 * @return this GameObject's parent
	 */
	public GameObject getParent() {
		return this.parent;
	}

	/**
	 * Sets the GameObject's parent. A GameObject can only be parented to another
	 * GameObject in the same Scene. Pass {@code null} to make the GameObject a root
	 * GameObject.
	 * 
	 * @param newParent the GameObject's parent, or {@code null}
	 * 
	 * @see GameObject#scene
	 * @see Scene#moveGameObjectTo(GameObject)
	 */
	public void setParent(GameObject parent) {
		if (parent != null && this.scene != parent.scene) {
			return; // this and newParent belong to different scenes.
		}

		if (parent != null && parent.isDescendantOf(this)) {
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
			if (this.isRoot()) {
				this.scene.rootGameObjects.remove(this);
			}

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
	 * @return {@code true} if this is a child of, or a child of a child, ect., of
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
	 * @param other GameObject to check if it's an ancestor of this
	 * @return {@code true} if other is an ancestor of this GameObject
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
	 * @return {@code true} if {@code other} and this GameObject are siblings
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
	 * @return {@code true} if this is a root GameObject
	 */
	public boolean isRoot() {
		return this.parent == null;
	}

	/**
	 * Gets the number of children this GameObject has.
	 * 
	 * @return the number of children
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
	 * Gets a child with a specific name.
	 * 
	 * @param name the name of the child
	 * @return the first child with name {@code name}, or {@code null} if there are
	 *         no child with that name
	 */
	public GameObject getChild(String name) {
		GameObject child;
		for (int i = 0; i < this.children.size(); i++) {
			child = this.children.get(i);
			if (child.getName().equals(name)) {
				return child;
			}
		}
		return null;
	}

	/**
	 * Gets the order of this GameObject in the hierarchy.
	 * 
	 * @return this GameObject's order in the hierarchy
	 */
	public int getIndexOf() {
		if (this.isRoot()) {
			return this.scene.getIndexOfRootGameObject(this);
		} else {
			int childCount = parent.getChildCount();
			for (int i = 0; i < childCount; i++) {
				if (parent.children.get(i) == this) {
					return i;
				}
			}

			return -1; // This should NEVER happen.
		}
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
			T component = type.getDeclaredConstructor().newInstance();
			component.owner = this;
			component.enabled = true;
			this.components.add(component);

			if (Application.isPlaying()) {
				component.invokeOnConstruct();
			}

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
	 * Gets a {@link JelloComponent} of the specific type.
	 * 
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
	 * Gets a {@link ArrayList} of {@link JelloComponent}s of a matching type on
	 * this GameObject. Child types of the passed type are also gotten.
	 * 
	 * @param type the type of {@link JelloComponent} to get
	 * @return a {@link List} of Components
	 * @throws IllegalArgumentException if type is null
	 */
	public <T extends JelloComponent> ArrayList<T> getComponents(Class<T> type) {
		Objects.requireNonNull(type, "type must not be null");

		ArrayList<T> components = new ArrayList<T>();

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
	 * @param type the type of {@link JelloComponent} to check for
	 * @return {@code true} if this GameObject has a component
	 * @throws IllegalArgumentException if type is null
	 */
	public <T extends JelloComponent> boolean hasComponent(Class<T> type) {
		Objects.requireNonNull(type, "type must not be null");

		for (JelloComponent component : this.components) {
			if (type.isInstance(component)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks to see how many {@link JelloComponent} of a type exist on a
	 * GameObject. Child types of the passed type are also counted.
	 * 
	 * @param type the type of {@link JelloComponent} to count
	 * @return the number of Components of the passed type on this GameObject
	 * @throws IllegalArgumentException if type is null
	 */
	public <T extends JelloComponent> int getComponentCount(Class<T> type) {
		Objects.requireNonNull(type, "type must not be null");

		int count = 0;
		for (JelloComponent component : this.components) {
			if (type.isInstance(component)) {
				count++;
			}
		}

		return count;
	}

	/**
	 * Removes all Components of the specified type from the GameObject.
	 * 
	 * @param type the type of {@link JelloComponent} to remove
	 * @return {@code true} if at least one Component was removed
	 * @throws IllegalArgumentException if type is null
	 */
	public <T extends JelloComponent> boolean removeComponent(Class<T> type) {
		Objects.requireNonNull(type, "type must not be null");

		int removedComponentCount = 0;
		for (int i = 0; i < this.components.size(); i++) {
			JelloComponent component = this.components.get(i);
			if (type.isInstance(component)) {
				component.destroy();
				this.components.remove(i);
				removedComponentCount++;
			}
		}

		return removedComponentCount > 0;
	}

	/**
	 * Removes a {@link JelloComponent} from the GameObject. If the
	 * {@link JelloComponent} is not on this GameObject, nothing happens.
	 * 
	 * @param component the {@link JelloComponent} to remove
	 * @return {@code true} if the component was removed
	 * @throws IllegalArgumentException if component is null
	 */
	protected boolean removeComponent(JelloComponent component) {
		Objects.requireNonNull(component, "component must not be null");

		int index = this.components.indexOf(component);
		if (index == -1) {
			return false;
		}

		this.components.remove(index);
		return true;
	}

	/**
	 * Gets the number of {@link JelloComponent} attached to this GameObject.
	 * 
	 * @return the number of attached Components
	 */
	public int getComponentCount() {
		return this.components.size();
	}

	/**
	 * Gets the {@link JelloComponent} at an index.
	 * 
	 * @param index the index to get the {@link JelloComponent} at
	 * @return the {@link JelloComponent} at the index
	 * @see GameObject#getComponentCount()
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *                                   ({@code index < 0 || index >= getComponentCount()})
	 */
	public JelloComponent getComponentAtIndex(int index) {
		return this.components.get(index);
	}

	/**
	 * Moves a {@link JelloComponent} up or down in the list of components. If the
	 * component can't be moved up or down because it is already at the front or end
	 * of the list respectively, nothing happens.
	 * 
	 * @param component the component to move
	 * @param direction the direction to move the component, -1 = toward the front
	 *                  of the list, 1 = toward the back of the list. Any other
	 *                  value does nothing
	 * @return {@code true} if the component was moved
	 * @throws IllegalArgumentException if component is null
	 */
	public boolean moveComponent(JelloComponent component, int direction) {
		Objects.requireNonNull(component, "type must not be null");

		if (!(direction == -1 || direction == 1)) {
			return false;
		}

		int index = this.components.indexOf(component);
		if (index == -1) {
			return false;
		}

		if (direction == -1) {
			if (index == 0) {
				return false; // Already at the front of the list.
			}
			this.components.remove(index);
			this.components.add(index - 1, component);
			return true;
		} else { // direction == 1
			if (index == this.components.size() - 1) {
				return false; // Already at the back of the list.
			}
			this.components.remove(index);
			this.components.add(index + 1, component);
			return true;
		}
	}

	public Iterable<JelloComponent> getAllComponents() {
		return this.components; // TODO document and protect return value
	}

	/**
	 * Checks if the GameObject is active. Even if this GameObject is active, it's
	 * Component's may not receive callbacks if this GameObject is not active within
	 * the Scene.
	 * 
	 * @return {@code true} if the GameObject is active
	 * @see GameObject#isActiveInScene()
	 */
	public boolean isActive() {
		return this.isActive;
	}

	/**
	 * Checks if the GameObject is active in it's Scene. For a GameObject to be
	 * active in it's Scene, it must be active itself and all of it's ancestors must
	 * be active as well.
	 * 
	 * @return {@code true} if the GameObject is active in it's Scene
	 */
	public boolean isActiveInScene() {
		if (this.parent != null) {
			return this.isActive && this.parent.isActiveInScene();
		} else {
			return this.isActive;
		}
	}

	/**
	 * Activates or deactivates the GameObject.
	 * 
	 * @param active should the GameObject be active
	 */
	public void setActive(boolean active) {
		if (this.isActive == active) {
			return; // Nothing changed.
		}

		if (Application.isPlaying()) {
			if (active) {
				this.isActive = true;
				this.invokeRecursively(this, (c) -> {
					GameObject obj = c.gameObject();
					if (obj.isActive && c.isEnabled()) {
						c.invokeOnEnable();
					}
				});
			} else {
				this.invokeRecursively(this, (c) -> {
					GameObject obj = c.gameObject();
					if (obj.isActive && c.isEnabled()) {
						c.invokeOnDisable();
					}
				});
				this.isActive = false;
			}
		} else {
			this.isActive = active;
		}
	}

	void invokeRecursively(GameObject obj, ILogic logic) {
		if (obj.isActiveInScene()) {
			for (int i = obj.getComponentCount() - 1; i >= 0; i--) {
				JelloComponent component = obj.getComponentAtIndex(i);
				logic.invoke(component);
			}

			for (int i = obj.getChildCount() - 1; i >= 0; i--) {
				GameObject child = obj.getChild(i);
				if (child.isActiveInScene()) {
					this.invokeRecursively(child, logic);
				}
			}
		}
	}

	private void invokeOnConstructIfInApplication() {
		if (Application.isPlaying()) {
			for (int i = 0; i < this.components.size(); i++) {
				this.components.get(i).invokeOnConstruct();
			}
		}
	}

	public interface ILogic {

		void invoke(JelloComponent component);
	}

}
