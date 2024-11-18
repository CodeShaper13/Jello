package com.codeshaper.jello.engine.event;

/**
 * {@link JelloEvent} with two arguments.
 * 
 * @param <T>
 */
public class JelloEvent2<T0, T1> extends JelloEventBase<JelloEvent2.IListener<T0, T1>> {

	public JelloEvent2() {
		super();
	}

	/**
	 * Invokes all listeners that have been added to the event.
	 * 
	 * @param arg0 the first argument
	 * @param arg1 the second argument
	 */
	public void invoke(T0 arg0, T1 arg1) {
		this.invokeMethodListeners(arg0, arg1);

		for (int i = this.listeners.size() - 1; i >= 0; i--) {
			this.listeners.get(i).perform(arg0, arg1);
		}
	}

	public static interface IListener<T0, T1> {

		void perform(T0 arg0, T1 arg1);
	}
}
