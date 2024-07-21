package com.codeshaper.jello.editor.window;

import javax.swing.JPanel;

import ModernDocking.Dockable;
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
}
