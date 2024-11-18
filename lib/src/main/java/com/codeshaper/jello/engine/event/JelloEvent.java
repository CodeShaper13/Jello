package com.codeshaper.jello.engine.event;

/**
 * Provides a way of creating an event driven system that is accessible in code
 * and in the Inspector.
 */
public class JelloEvent extends JelloEventBase<JelloEvent.IListener> {

	public JelloEvent() {
		super();
	}

	/**
	 * Invokes all listeners that have been added to the event.
	 * 
	 * @param argument
	 */
	public void invoke() {
		this.invokeMethodListeners();

		for (int i = this.listeners.size() - 1; i >= 0; i--) {
			this.listeners.get(i).perform();
		}
	}

	public static interface IListener {

		void perform();
	}
}
