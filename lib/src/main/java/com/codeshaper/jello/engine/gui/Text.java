package com.codeshaper.jello.engine.gui;

import com.codeshaper.jello.engine.ComponentIcon;
import com.codeshaper.jello.engine.ComponentName;
import com.codeshaper.jello.engine.asset.Font;

@ComponentName("Ui/Element/Text")
@ComponentIcon("/editor/componentIcons/text.png")
public class Text extends UiBase {

	public String text = "Text";
	public Font font = null;
}
