package com.codeshaper.jello.editor.window;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import com.codeshaper.jello.editor.inspector.Editor;
import com.codeshaper.jello.editor.inspector.IInspectable;

import ModernDocking.Dockable;
import ModernDocking.app.Docking;

public class InspectorWindow extends EditorWindow {
	
	private IInspectable target;
	private Editor<?> editor;
	private JPanel panel;
	
    public InspectorWindow() {    	
    	super("Inspector", "inspector");
        
        this.panel = new JPanel();
        this.setLayout(new BorderLayout());
    }
    
    @Override
    public boolean isWrappableInScrollpane() {
    	return false;
    }
    
    @Override
    public boolean getHasMoreOptions() {
    	return true;
    }
    
    @Override
    public void addMoreOptions(JPopupMenu menu) {
    	JMenuItem refresh = new JMenuItem("Refresh");
    	refresh.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
            	refresh();
            }});
    	menu.add(refresh);
    }
    
    /**
     * Sets the object that the inspector is looking at.
     * 
     * @param object
     */
    public void setTarget(IInspectable object) {
    	// Let the previous editor perform any cleanup that it needs to do.
    	if(this.editor != null) {
    		this.editor.cleanup();
    		this.editor = null;
    	}
    	
    	this.remove(this.panel);
    	this.panel = new JPanel();
		this.add(this.panel, BorderLayout.CENTER);
		    	
    	this.target = object;
    	if(this.target != null) {
    		// Create a new editor.
    		this.editor = this.target.getInspectorDrawer();
    		this.editor.draw(this.panel);
    	}
    	
    	this.validate();
    }
    
    /**
     * Gets the object that the inspector is inspecting.  May be null.
     * @return
     */
    public IInspectable getTarget() {
    	return this.target;
    }
    
    public void refresh() {
    	if(this.editor != null) {
    		this.editor.refresh();
    	}
    }
}
