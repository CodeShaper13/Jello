package com.codeshaper.jello.editor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * Provides a set of properties that can be access by a string key.
 */
public class EditorProperties {

	/**
	 * The file holding the properties.
	 */
	public final File file;

	private final Properties props;

	public EditorProperties(File file) {
		this.file = file;
		this.createFileIfMissing();

		this.props = new Properties();

		try (FileInputStream stream = new FileInputStream(file)) {
			this.props.load(stream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Saves the properties.
	 */
	public void save() {
		this.createFileIfMissing();

		try (FileOutputStream outputStream = new FileOutputStream(this.file)) {
			this.props.store(outputStream, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets a property as a {@link string}. If the property does not exist, the
	 * default value is returned.
	 * 
	 * @param key          the property key
	 * @param defaultValue a default value
	 * @return the property
	 */
	public String getString(String key, String defaultValue) {
		String value = this.props.getProperty(key);
		return value != null ? value : defaultValue;
	}

	/**
	 * Gets a property as a {@link boolean}. If the property does not exist, or it
	 * is not {@code "true"} or {@code "false"}, the default value is returned.
	 * 
	 * @param key          the property key
	 * @param defaultValue a default value
	 * @return the property
	 */
	public boolean getBoolean(String key, boolean defaultValue) {
		String value = this.props.getProperty(key);
		if (value == null) {
			return defaultValue;
		}
		if (value.equalsIgnoreCase("true")) {
			return true;
		} else if (value.equalsIgnoreCase("false")) {
			return false;
		} else {
			return defaultValue;
		}
	}

	/**
	 * Gets a property as a {@link byte}. If the property does not exist, or it is
	 * not a {@link byte}, the default value is returned.
	 * 
	 * @param key          the property key
	 * @param defaultValue a default value
	 * @return the property
	 */
	public byte getByte(String key, byte defaultValue) {
		return NumberUtils.toByte(this.props.getProperty(key), defaultValue);
	}

	/**
	 * Gets a property as a {@link short}. If the property does not exist, or it is
	 * not a {@link short}, the default value is returned.
	 * 
	 * @param key          the property key
	 * @param defaultValue a default value
	 * @return the property
	 */
	public short getShort(String key, short defaultValue) {
		return NumberUtils.toShort(this.props.getProperty(key), defaultValue);
	}

	/**
	 * Gets a property as a {@link int}. If the property does not exist, or it is
	 * not a {@link int}, the default value is returned.
	 * 
	 * @param key          the property key
	 * @param defaultValue a default value
	 * @return the property
	 */
	public int getInt(String key, int defaultValue) {
		return NumberUtils.toInt(this.props.getProperty(key), defaultValue);
	}

	/**
	 * Gets a property as a {@link long}. If the property does not exist, or it is
	 * not a {@link long}, the default value is returned.
	 * 
	 * @param key          the property key
	 * @param defaultValue a default value
	 * @return the property
	 */
	public long getLong(String key, long defaultValue) {
		return NumberUtils.toLong(this.props.getProperty(key), defaultValue);
	}

	/**
	 * Gets a property as a {@link float}. If the property does not exist, or it is
	 * not a {@link float}, the default value is returned.
	 * 
	 * @param key          the property key
	 * @param defaultValue a default value
	 * @return the property
	 */
	public float getFloat(String key, float defaultValue) {
		return NumberUtils.toFloat(this.props.getProperty(key), defaultValue);
	}

	/**
	 * Gets a property as a {@link double}. If the property does not exist, or it is
	 * not a {@link double}, the default value is returned.
	 * 
	 * @param key          the property key
	 * @param defaultValue a default value
	 * @return the property
	 */
	public double getDouble(String key, double defaultValue) {
		return NumberUtils.toDouble(this.props.getProperty(key), defaultValue);
	}

	public void setString(String key, String value) {
		this.props.setProperty(key, value);
	}

	public void setBoolean(String key, boolean value) {
		this.props.setProperty(key, Boolean.toString(value));
	}

	public void setByte(String key, byte value) {
		this.props.setProperty(key, Byte.toString(value));
	}

	public void setShort(String key, short value) {
		this.props.setProperty(key, Short.toString(value));
	}

	public void setInt(String key, int value) {
		this.props.setProperty(key, Integer.toString(value));
	}

	public void setLong(String key, long value) {
		this.props.setProperty(key, Long.toString(value));
	}

	public void setFloat(String key, float value) {
		this.props.setProperty(key, Float.toString(value));
	}

	public void setDouble(String key, double value) {
		this.props.setProperty(key, Double.toString(value));
	}

	/**
	 * Removes a property.
	 * 
	 * @param key the key of the property to remove.
	 */
	public void remove(String key) {
		this.props.remove(key);
	}

	/**
	 * Checks if a property exists with a specific key.
	 * 
	 * @param key the key of the property.
	 * @return {@code true} if the property exists.
	 */
	public boolean exists(String key) {
		return this.props.containsKey(key);
	}

	/**
	 * Creates the properties file if it does not exist.
	 */
	private void createFileIfMissing() {
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
