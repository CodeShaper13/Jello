package com.codeshaper.jello.editor.property.modifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.lang3.StringUtils;

import com.codeshaper.jello.engine.asset.SerializedJelloObject;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CreateAssetEntry {

	static final String DEFAULT_ASSET_NAME = "Asset";
	static final String DEFAULT_MENU_LOCATION = "";

	public String fileName() default DEFAULT_ASSET_NAME;

	public String location() default DEFAULT_MENU_LOCATION;
}
