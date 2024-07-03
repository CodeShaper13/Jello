package com.codeshaper.jello.engine.component;

import com.codeshaper.jello.editor.property.modifier.MinValue;
import com.codeshaper.jello.editor.property.modifier.TextBox;
import com.codeshaper.jello.engine.Color;
import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.asset.Font;

public class FontRenderer extends JelloComponent {

	public Font font;
	@TextBox(3)
	public String text = "Text";
	@MinValue(0)
	public int size = 14;
	public Color color = Color.black;
	
	public FontRenderer(GameObject owner) {
		super(owner);
	}
}
