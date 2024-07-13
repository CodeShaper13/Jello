package com.codeshaper.jello.editor.window;

import javax.swing.JPanel;

import org.apache.commons.lang3.math.NumberUtils;

import ModernDocking.Dockable;
import ModernDocking.app.AppState;
import ModernDocking.app.Docking;

public class EditorWindow extends JPanel implements Dockable {

	protected final String displayName;
	protected final String internalName;
	
	public EditorWindow(String displayName, String internalName) {
		this.displayName = displayName;
		this.internalName = internalName;
		
		Docking.registerDockable(this);
	}

	@Override
	public String getTabText() {
		return this.displayName;
	}
	
	@Override
	public String getPersistentID() {
		return this.internalName;
	}
	
	public String getStringProperty(String key, String defaultValue) {
		String value = AppState.getProperty(this, key);
		return value != null ? value : defaultValue;
	}

	public boolean getPropertyBoolean(String key, boolean defaultValue) {
		String value = AppState.getProperty(this, key);
		if(value == null) {
			return defaultValue;
		}
		if(value.equalsIgnoreCase("true")) {
			return true;
		} else if(value.equalsIgnoreCase("false")) {
			return false;
		} else {
			return defaultValue;
		}
	}

	public byte getPropertyByte(String key, byte defaultValue) {
		return NumberUtils.toByte(AppState.getProperty(this, key), defaultValue);
	}
	
	public short getPropertyShort(String key, short defaultValue) {
		return NumberUtils.toShort(AppState.getProperty(this, key), defaultValue);
	}
	
	public int getPropertyInt(String key, int defaultValue) {
		return NumberUtils.toInt(AppState.getProperty(this, key), defaultValue);
	}

	public long getPropertyLong(String key, long defaultValue) {
		return NumberUtils.toLong(AppState.getProperty(this, key), defaultValue);
	}
	
	public float getPropertyFloat(String key, float defaultValue) {
		return NumberUtils.toFloat(AppState.getProperty(this, key), defaultValue);
	}
	
	public double getPropertyDouble(String key, double defaultValue) {
		return NumberUtils.toDouble(AppState.getProperty(this, key), defaultValue);
	}
	
	public void setProperty(String key, String value) {
		AppState.setProperty(this, key, value);
	}

	public void setProperty(String key, boolean value) {
		AppState.setProperty(this, key, Boolean.toString(value));
	}

	public void setProperty(String key, byte value) {
		AppState.setProperty(this, key, Byte.toString(value));
	}
	
	public void setProperty(String key, short value) {
		AppState.setProperty(this, key, Short.toString(value));
	}

	public void setProperty(String key, int value) {
		AppState.setProperty(this, key, Integer.toString(value));
	}
	
	public void setProperty(String key, long value) {
		AppState.setProperty(this, key, Long.toString(value));
	}
	
	public void setProperty(String key, float value) {
		AppState.setProperty(this, key, Float.toString(value));
	}
	
	public void setProperty(String key, double value) {
		AppState.setProperty(this, key, Double.toString(value));
	}
}
