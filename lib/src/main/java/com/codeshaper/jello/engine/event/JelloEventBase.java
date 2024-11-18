package com.codeshaper.jello.engine.event;

import java.util.ArrayList;
import java.util.List;

abstract class JelloEventBase<T> {

	protected transient List<T> listeners;
	
	/**
	 * This is what's displayed in the Inspector.
	 */
	private List<ListenerJavaMethod> javaMethods; // Don't renamed, accessed by JelloEventDrawer
	
	public JelloEventBase() {
		this.listeners = new ArrayList<T>();
		this.javaMethods = new ArrayList<ListenerJavaMethod>();
	}
	
	/**
	 * Add a listener to the Event. If {@code listener} is null, this is no-op.
	 * 
	 * @param listener the listener to add to this Event
	 */
	public void addListener(T listener) {
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
	public boolean removeListener(T listener) {
		if (listener == null) {
			return false;
		} else {
			return this.listeners.remove(listener);
		}
	}
	
	/**
	 * Removes all listeners from the Event.
	 */
	public void clear() {
		this.listeners.clear();
	}
	
	/**
	 * Checks if a listener has been added to this Event.
	 * 
	 * @param listener the listener to check for
	 * @return {@code true} if the listener has been added.
	 */
	public boolean hasListener(T listener) {
		return this.listeners.contains(listener);
	}
	
	protected void invokeMethodListeners(Object... args) {
		for (int i = this.javaMethods.size() - 1; i >= 0; i--) {
			this.javaMethods.get(i).invoke();
		}
	}
}
