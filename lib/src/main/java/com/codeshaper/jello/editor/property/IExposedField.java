package com.codeshaper.jello.editor.property;

import java.lang.annotation.Annotation;

public interface IExposedField {

	/**
	 * 
	 * @return
	 */
	public String getFieldName();
	
	public Class<?> getType();

	public Object get();
	
	public boolean set(Object value);
	
	public boolean isReadOnly();
	
	public <T extends Annotation> T getAnnotation(Class<T> annotationType);
	
	public IExposedField getSubProperty(String name);
}
