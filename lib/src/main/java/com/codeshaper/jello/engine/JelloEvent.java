package com.codeshaper.jello.engine;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class JelloEvent {

	private transient List<IJelloEventListener> listeners;
	/**
	 * This is what's displayed in the Inspector.
	 */
	private List<MethodListener> callbacks; // Don't renamed, accessed by JelloEventDrawer

	public JelloEvent() {
		this.listeners = new ArrayList<IJelloEventListener>();
		this.callbacks = new ArrayList<MethodListener>();
	}

	/**
	 * Invokes all listeners that have been added to the event.
	 * 
	 * @param argument
	 */
	public void invoke() {
		for (int i = this.callbacks.size() - 1; i >= 0; i--) {
			this.callbacks.get(i).perform();
		}

		for (int i = this.listeners.size() - 1; i >= 0; i--) {
			this.listeners.get(i).perform();
		}
	}

	/**
	 * Add a listener to the Event. If {@code listener} is null, this is no-op.
	 * 
	 * @param listener the listener to add to this Event
	 */
	public void addListener(IJelloEventListener listener) {
		if (listener != null) {
			this.listeners.add(listener);
		}
	}

	/**
	 * Removes a listener from the Event. If {@code listener} is null, this is
	 * no-op.
	 * 
	 * @return {@code true} if a listener was removed
	 * @param listener the listener to remove from this Event
	 */
	public boolean removeListener(IJelloEventListener listener) {
		if (listener == null) {
			return false;
		} else {
			return this.listeners.remove(listener);
		}
	}

	/**
	 * Checks if a listener has been added to this Event.
	 * 
	 * @param listener the listener to check for
	 * @return {@code true} if the listener has been added.
	 */
	public boolean hasListener(IJelloEventListener listener) {
		return this.listeners.contains(listener);
	}

	/**
	 * Removes all listeners from the Event.
	 */
	public void clear() {
		this.listeners.clear();
	}
	
	public class BaseEvent {
		
	}
	
	public class Event {
		
		public void addListener() { }
		
		public void invoke() { }
	}
	
	public class Event1<T> {
		
		public void invoke(T arg) { }
	}
	
	public class Event2<T0, T1> {
		
		public void invoke(T0 arg0, T1 arg1) { }
	}
	
	public interface IListener {
		
	}
	
	private void func() {
		
	}

	public static class MethodListener implements IJelloEventListener {

		private GameObjectReference reference;
		public Method method;

		public MethodListener() {
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


		@Override
		public void perform() {
			GameObject obj = this.reference.get();
			if (obj != null && this.method != null) {
				try {
					method.invoke(obj);
				} catch (Exception e) {
					Debug.log(e);
				}
			}
		}
	}
}
