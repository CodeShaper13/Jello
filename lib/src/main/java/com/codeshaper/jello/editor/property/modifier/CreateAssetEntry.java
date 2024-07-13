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

	public class Data {

		private final String fileName;
		private final String menuName;
		public final Class<? extends SerializedJelloObject> clazz;

		public Data(String fileName, String location, Class<? extends SerializedJelloObject> clazz) {
			this.fileName = fileName;
			this.menuName = location;
			this.clazz = clazz;
		}
		
		public String getMenuName() {
			if(StringUtils.isBlank(this.menuName)) {
				return this.clazz.getName();
			} else {
				return this.menuName;
			}
		}
		
		/**
		 * Gets the assets default name without it's extension.
		 * @return
		 */
		public String getNewAssetName() {
			String fileName;
			if(StringUtils.isBlank(this.fileName)) {
				fileName = this.clazz.getName().toLowerCase();
			} else {
				fileName = this.fileName;
			}
			return fileName;
		}
	}
}
