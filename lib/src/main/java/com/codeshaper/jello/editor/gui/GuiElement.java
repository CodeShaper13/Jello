package com.codeshaper.jello.editor.gui;

import javax.swing.JComponent;

public class GuiElement<T extends JComponent> {
	
	protected final T backingComponent;

	protected GuiElement(T backingComponent) {
		this.backingComponent = backingComponent;
	}
	
	public GuiElement<T> setTooltip(String text) {
		this.backingComponent.setToolTipText(text);
		return this;
	}
	
	public GuiElement<T> setDisabled(boolean disabled) {
		this.backingComponent.setEnabled(!disabled);
		return this;
	}
}
