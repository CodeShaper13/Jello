package com.codeshaper.jello.engine.gui;

import com.codeshaper.jello.editor.property.modifier.ExposeField;
import com.codeshaper.jello.engine.ComponentIcon;
import com.codeshaper.jello.engine.ComponentName;
import com.codeshaper.jello.engine.event.JelloEvent;

@ComponentName("Ui/Element/Button")
@ComponentIcon("/_editor/componentIcons/button.png")
public class Button extends UiBase {

	
	@ExposeField
	private boolean isEnabled;
	public JelloEvent onClick;
}
