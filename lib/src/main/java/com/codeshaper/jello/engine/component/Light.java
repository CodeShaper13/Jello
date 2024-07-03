package com.codeshaper.jello.engine.component;

import com.codeshaper.jello.editor.property.modifier.MinValue;
import com.codeshaper.jello.editor.property.modifier.ShowIf;
import com.codeshaper.jello.engine.Color;
import com.codeshaper.jello.engine.GameObject;

public class Light extends JelloComponent {

	public EnumLightType lightType = EnumLightType.POINT;
	public Color color = Color.white;
	@MinValue(0f)
	public float range = 10f;
	@MinValue(0f)
	@ShowIf("isSpotLight")
	public float coneAngle;
	@MinValue(0f)
	public float brightness = 1f;
	boolean castShadows = true;
	
	public Light(GameObject owner) {
		super(owner);
	}
	
	@SuppressWarnings("unused")
	private boolean isSpotLight() {
		return this.lightType == EnumLightType.SPOT;
	}
	
	public enum EnumLightType {
		POINT,
		DIRECTIONAL,
		SPOT,
	}
}
