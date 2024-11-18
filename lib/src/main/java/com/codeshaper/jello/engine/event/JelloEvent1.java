package com.codeshaper.jello.engine.event;

/**
 * {@link JelloEvent} with one argument.
 * 
 * @param <T>
 */
public class JelloEvent1<T> extends JelloEventBase<JelloEvent1.IListener<T>> {

	public JelloEvent1() {
		super();
	}

	/**
	 * Invokes all listeners that have been added to the event.
	 * 
	 * @param arg
	 */
	public void invoke(T arg) {
		this.invokeMethodListeners(arg);

		for (int i = this.listeners.size() - 1; i >= 0; i--) {
			this.listeners.get(i).perform(arg);
		}
	}

	public static interface IListener<T> {

		void perform(T arg);
	}
}
