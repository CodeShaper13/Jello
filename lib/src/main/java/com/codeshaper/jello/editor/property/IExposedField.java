package com.codeshaper.jello.editor.property;

import java.lang.annotation.Annotation;

import com.codeshaper.jello.editor.property.modifier.ReadOnly;

public interface IExposedField {

	/**
	 * Gets the name of the field. For array elements, "Element index" is returned,
	 * with index being the fields index in the array.
	 * 
	 * @return
	 */
	public String getFieldName();

	/**
	 * Gets the type of the exposed field.
	 * 
	 * @return
	 */
	public Class<?> getType();

	/**
	 * Gets the value of the field.
	 * 
	 * @return
	 */
	public Object get();

	/**
	 * Sets the value of the field. If the value is of the wrong type, nothing
	 * happens.
	 * 
	 * @param value
	 * @return
	 */
	public boolean set(Object value);

	/**
	 * Checks if the field is marked as Read Only by the {@link ReadOnly}
	 * annotation.
	 * 
	 * @return {@link true} if the field is read only, {@link false} if it is not.
	 */
	public boolean isReadOnly();

	/**
	 * Gets an annotation on the field. If this field is representing an array
	 * element, the array is checked for the annotation.
	 * 
	 * @param <T>
	 * @param annotationType
	 * @return an annotation, or null if it isn't present.
	 */
	public <T extends Annotation> T getAnnotation(Class<T> annotationType);

	public IExposedField getSubProperty(String name);
}
