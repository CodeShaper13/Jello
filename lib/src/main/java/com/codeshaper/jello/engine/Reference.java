package com.codeshaper.jello.engine;

public class Reference<T> {

	private T reference;
	
	public Reference() {
		
	}
	
	public T get() {
		return null;
	}
	
	public void set(T value) {
		this.reference = value;
	}
	
	public boolean isSet() {
		return this.reference != null;		
	}
}
