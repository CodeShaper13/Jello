package com.codeshaper.jello.editor.gui;

import javax.swing.JToggleButton;

public class GuiElementToggle extends GuiElement<JToggleButton> {

	protected GuiElementToggle(JToggleButton backingComponent) {
		super(backingComponent);
	}
	
	public boolean isSelected() {
		return this.backingComponent.isSelected();
		
	}
	
	public void setSelected(boolean selected) {
		this.backingComponent.setSelected(selected);
	}
}
