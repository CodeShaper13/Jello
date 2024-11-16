package com.codeshaper.jello.engine;

/**
 * Provides a means of creating a reference to a GameObject in the Inspector.
 */
public class GameObjectReference {

	private String persistantPath;
	private transient GameObject gameObj;

	/**
	 * Creates a {@link GameObjectReference} that doesn't point to a
	 * {@link GameObject}.
	 */
	public GameObjectReference() {
		// Do nothing.
	}

	/**
	 * Creates a {@link GameObjectReference} that points to a {@link GameObject} at the
	 * specified path.
	 * 
	 * @param persistantPath the path to the {@link GameObject}. Can safely by null or
	 *                       empty.
	 * @see GameObject#getPersistencePath()
	 */
	public GameObjectReference(String persistantPath) {
		this.gameObj = GameObject.fromPersistantPath(persistantPath);
	}

	/**
	 * Creates a {@link GameObjectReference} that points to a {@link GameObject}.
	 * 
	 * @param target the GameObject that this reference points to.  May be null.
	 */
	public GameObjectReference(GameObject target) {
		this.gameObj = target;
	}

	/**
	 * Gets the GameObject that this reference points to.
	 * 
	 * @return
	 */
	public GameObject get() {
		if (this.gameObj != null) {
			return this.gameObj;
		} else {
			// Lookup GameObject.
			return GameObject.fromPersistantPath(this.persistantPath);
		}
	}

	/**
	 * Sets the GameObject that this reference points to.
	 * 
	 * @param value the {@link GameObject} this reference should point to.
	 */
	public void set(GameObject value) {
		this.gameObj = value;
		if (value != null) {
			this.persistantPath = value.getPersistencePath();
		} else {
			this.persistantPath = null;
		}
	}
}
