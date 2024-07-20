package com.codeshaper.jello.editor.property;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import com.codeshaper.jello.editor.EditorUtil;
import com.codeshaper.jello.editor.property.modifier.DisableIf;
import com.codeshaper.jello.editor.property.modifier.ReadOnly;

public class ExposedField implements IExposedField {

	public final Object instance;
	public final Field backingField;
	
	public ExposedField(Object instance, String fieldName) {
		this.instance = instance;

		Field field = null;
		try {
			field = instance.getClass().getDeclaredField(fieldName);
			field.trySetAccessible();
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		
		this.backingField = field;
	}

	public ExposedField(Object instance, Field field) {
		this.instance = instance;
		
		field.trySetAccessible();
		this.backingField = field;
	}

	@Override
	public String getFieldName() {
		return this.backingField.getName();
	}

	@Override
	public Class<?> getType() {
		return this.backingField.getType();
	}
	
	@Override
	public Object get() {
		try {
			if(this.backingField.canAccess(this.instance)) {
				return this.backingField.get(this.instance);
			} else {
				System.out.println("Can't get field value, it is inaccessible.");
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public boolean set(Object value) {
		try {
			if(this.backingField.canAccess(this.instance)) {
				this.backingField.set(this.instance, value);
				return true;
			} else {
				System.out.println("Can't set field value, it is inaccessible.");
				return false;
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}

		return false;
	}
	
	@Override
	public boolean isReadOnly() {
		if(this.backingField.isAnnotationPresent(ReadOnly.class)) {
			return true;
		}
		
		DisableIf disableIf = this.getAnnotation(DisableIf.class);
		if(disableIf != null) {
			try {
				boolean value = EditorUtil.invokeMethod(this, disableIf.value());
				return value;
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				// TODO what should we do with these errors?
			}
		}
		
		return false;
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
		return this.backingField.getAnnotation(annotationType);
	}
	
	@Override
	public ExposedField getSubProperty(String name) {
		try {
			Field subField = this.backingField.getType().getField(name);
			Object instance = this.backingField.get(this.instance);
			return new ExposedField(instance, subField);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			// Swallow.
		}

		return null;
	}
}
