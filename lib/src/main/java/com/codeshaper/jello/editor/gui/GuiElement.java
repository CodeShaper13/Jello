package com.codeshaper.jello.editor.gui;

import javax.swing.JComponent;

public class GuiElement {
	
	protected final JComponent backingComponent;

	protected GuiElement(JComponent backingComponent) {
		this.backingComponent = backingComponent;
	}
	
	public GuiElement setTooltip(String text) {
		this.backingComponent.setToolTipText(text);
		return this;
	}
	
	public GuiElement setDisabled(boolean disabled) {
		this.backingComponent.setEnabled(!disabled);
		return this;
	}
}
