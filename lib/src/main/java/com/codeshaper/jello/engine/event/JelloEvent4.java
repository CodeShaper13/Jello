package com.codeshaper.jello.engine.event;

/**
 * {@link JelloEvent} with four arguments.
 * 
 * @param <T>
 */
public class JelloEvent4<T0, T1, T2, T3> extends JelloEventBase<JelloEvent4.IListener<T0, T1, T2, T3>> {

	public JelloEvent4() {
		super();
	}

	/**
	 * Invokes all listeners that have been added to the event.
	 * 
	 * @param arg0 the first argument
	 * @param arg1 the second argument
	 * @param arg2 the third argument
	 * @param arg3 the fourth argument
	 */
	public void invoke(T0 arg0, T1 arg1, T2 arg2, T3 arg3) {
		this.invokeMethodListeners(arg0, arg1, arg2, arg3);

		for (int i = this.listeners.size() - 1; i >= 0; i--) {
			this.listeners.get(i).perform(arg0, arg1, arg2, arg3);
		}
	}

	public static interface IListener<T0, T1, T2, T3> {

		void perform(T0 arg0, T1 arg1, T2 arg2, T3 arg3);
	}
}