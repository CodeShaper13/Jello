package com.codeshaper.jello.editor.property;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

import com.codeshaper.jello.editor.property.modifier.ReadOnly;

public class ExposedArrayField implements IExposedField {

	private final Object instance;
	private final Field arrayField;
	private final int index;
	
	public ExposedArrayField(Field arrayField, Object arrayInstance, int index) {
		this.instance = arrayInstance;
		this.arrayField = arrayField;
		this.index = index;
	}
	
	@Override
	public String getFieldName() {
		// TODO Auto-generated method stub
		return "Element " + this.index;
	}
	
	@Override
	public Class<?> getType() {
		return this.arrayField.getType().getComponentType();
	}

	@Override
	public Object get() {
		return Array.get(this.instance, this.index);
	}

	@Override
	public boolean set(Object value) {
		Array.set(this.instance, this.index, value);
		return true; // TODO
	}

	@Override
	public boolean isReadOnly() {
		return this.arrayField.isAnnotationPresent(ReadOnly.class);
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
		// Array elements never have annotations.
		return null;
	}

	@Override
	public IExposedField getSubProperty(String name) {
		return null;
	}
}
