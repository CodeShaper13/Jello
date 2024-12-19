package com.codeshaper.jello.engine.gui;

import com.codeshaper.jello.editor.property.modifier.ExposeField;
import com.codeshaper.jello.engine.Color;
import com.codeshaper.jello.engine.ComponentIcon;
import com.codeshaper.jello.engine.ComponentName;
import com.codeshaper.jello.engine.asset.Font;

@ComponentName("Ui/Element/Text")
@ComponentIcon("/_editor/componentIcons/text.png")
public class Text extends UiBase {

	@ExposeField
	private String text = "Text";
	@ExposeField
	private Font font = null;
	
	public String getText() {
		return this.text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public Font getFont() {
		return this.font;
	}
	
	public void setFont(Font font) {
		this.font = font;
	}
}
