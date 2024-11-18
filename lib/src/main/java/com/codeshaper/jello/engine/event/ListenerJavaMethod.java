package com.codeshaper.jello.engine.event;

import java.lang.reflect.Method;

import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.GameObjectReference;

/**
 * Internally used to let {@link Method}s be listeners for {@link JelloEvent}s.
 */
public class ListenerJavaMethod {

	public Method method;

	private GameObjectReference reference;

	public ListenerJavaMethod() {
		this.reference = new GameObjectReference();
	}

	public GameObject getGameObject() {
		return this.reference != null ? this.reference.get() : null;
	}

	public void setGameObject(GameObject obj) {
		if (this.reference.get() == obj) {
			return;
		}

		this.reference.set(obj);
		this.method = null;
	}

	public void invoke(Object... args) {
		Object obj = this.reference.get();
		if (obj != null && this.method != null) {
			try {
				method.invoke(obj, args);
			} catch (Exception e) {
				Debug.log(e);
			}
		}
	}
}