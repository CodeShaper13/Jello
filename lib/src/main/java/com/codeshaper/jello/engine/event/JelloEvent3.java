package com.codeshaper.jello.engine.event;

/**
 * {@link JelloEvent} with three arguments.
 * 
 * @param <T>
 */
public class JelloEvent3<T0, T1, T2> extends JelloEventBase<JelloEvent3.IListener<T0, T1, T2>> {

	public JelloEvent3() {
		super();
	}

	/**
	 * Invokes all listeners that have been added to the event.
	 * 
	 * @param arg0 the first argument
	 * @param arg1 the second argument
	 * @param arg2 the third argument
	 */
	public void invoke(T0 arg0, T1 arg1, T2 arg2) {
		this.invokeMethodListeners(arg0, arg1, arg2);

		for (int i = this.listeners.size() - 1; i >= 0; i--) {
			this.listeners.get(i).perform(arg0, arg1, arg2);
		}
	}

	public static interface IListener<T0, T1, T2> {

		void perform(T0 arg0, T1 arg1, T2 arg2);
	}
}