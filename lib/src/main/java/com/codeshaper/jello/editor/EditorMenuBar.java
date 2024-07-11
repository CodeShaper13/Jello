package com.codeshaper.jello.editor;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import com.codeshaper.jello.engine.Application;
import com.codeshaper.jello.engine.Debug;

import ModernDocking.Dockable;
import ModernDocking.app.DockableMenuItem;
import ModernDocking.app.Docking;
import ModernDocking.app.LayoutsMenu;

public class EditorMenuBar extends JMenuBar {
	
	private final String reportIssueUrl;
	private final String documentationUrl;
		
	public EditorMenuBar(String reportIssueUrl, String documentationUrl) {	
		this.reportIssueUrl = reportIssueUrl;
		this.documentationUrl = documentationUrl;
		
        this.add(this.createFileMenu());
        this.add(this.createEditMenu());
        this.add(this.createRunMenu());
        this.add(this.createWindowMenu());
        this.add(this.createHelpMenu());
	}
	
    private JMenu createFileMenu() {
        JMenu fileMenu = new JMenu("File");
        
        JMenuItem save = new JMenuItem("Save Open Scene");
    	save.setAccelerator(KeyStroke.getKeyStroke(
    			KeyEvent.VK_S, ActionEvent.CTRL_MASK));
    	save.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
            	JelloEditor.instance.saveScene();
            }});
        fileMenu.add(save);
        
        JMenuItem openInExplorer = new JMenuItem("Open In Explorer");
        openInExplorer.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
            	Path assetsFolder = JelloEditor.instance.rootProjectFolder;
            	try {
					Desktop.getDesktop().open(assetsFolder.toFile());
				} catch (IOException e1) {
					Debug.logWarning("Couldn't open %s", assetsFolder);
				}
            }});
        fileMenu.add(openInExplorer);
       
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
            	System.exit(0);
            }});
        fileMenu.add(exit);
        
        return fileMenu;
    }
    
	private JMenu createEditMenu() {
        JMenu menu = new JMenu("Edit");
        
        JMenuItem undo = new JMenuItem("Undo");
    	undo.setAccelerator(KeyStroke.getKeyStroke(
    			KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
    	undo.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
            	JelloEditor.instance.preformUndo();
            }});
        menu.add(undo);

        JMenuItem redo = new JMenuItem("Redo");
    	redo.setAccelerator(KeyStroke.getKeyStroke(
    			KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
    	redo.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
            	JelloEditor.instance.preformRedo();
            }});
        menu.add(redo);
        
        menu.addSeparator();
        
        JMenuItem cut = new JMenuItem("Cut");
    	cut.setAccelerator(KeyStroke.getKeyStroke(
    			KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        menu.add(cut);
        
        JMenuItem copy = new JMenuItem("Copy");
    	copy.setAccelerator(KeyStroke.getKeyStroke(
    			KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        menu.add(copy);
        
        JMenuItem paste = new JMenuItem("Paste");
    	paste.setAccelerator(KeyStroke.getKeyStroke(
    			KeyEvent.VK_P, ActionEvent.CTRL_MASK));
        menu.add(paste);
       
        return menu;
    }
    	
    private JMenu createRunMenu() {
    	JMenu menu = new JMenu("Run");    	

    	JMenuItem run = new JMenuItem("Run");
    	run.setToolTipText("Runs the Scene that is set as the \"Main Scene\"");
    	run.setAccelerator(KeyStroke.getKeyStroke(
    			KeyEvent.VK_R, ActionEvent.CTRL_MASK));
    	run.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
        		new Application().start(); // TODO start in the correct scene.
            }});
    	menu.add(run);
    	
    	JMenuItem runCurrent = new JMenuItem("Run Current");
    	runCurrent.setToolTipText("Runs the currently open Scene");
    	runCurrent.setAccelerator(KeyStroke.getKeyStroke(
    			KeyEvent.VK_R, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
    	runCurrent.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
            	new Application().start(); // TODO start in the correct scene.
            }});
    	menu.add(runCurrent);
    	
    	return menu;
    }
    
    private JMenu createWindowMenu() {
    	JMenu menu = new JMenu("Window");
    	
		LayoutsMenu layouts = new LayoutsMenu();
		menu.add(layouts);
		
		for(Dockable dockable : Docking.getDockables()) {
			menu.add(new DockableMenuItem(
					dockable.getPersistentID(),
					"Open " + dockable.getTabText()));
		}
		
		/*
        JMenuItem reportIssue = new JMenuItem("Open Inspector Window");
        reportIssue.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
        		Docking.newWindow("inspector");
            }});
        menu.add(reportIssue);
		*/
		    	
    	return menu;
    }
    
	private JMenu createHelpMenu() {
        JMenu help = new JMenu("Help");
        
        JMenuItem documentation = new JMenuItem("Documentaion");
        documentation.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
            	openUri(documentationUrl);
            }});
        help.add(documentation);
        
        JMenuItem reportIssue = new JMenuItem("Report Issue");
        reportIssue.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
            	openUri(reportIssueUrl);
            }});
        help.add(reportIssue);
        
        JMenuItem about = new JMenuItem("About");
        about.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
            	String message = String.format("Version: {0}", JelloEditor.EDITOR_VERSION);
            	JOptionPane.showMessageDialog(
            			getParent(), message, "Version", JOptionPane.INFORMATION_MESSAGE);
            }});
        help.add(about);
        
        return help;
    }
	
	private void openUri(String url) {
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
		    try {
				Desktop.getDesktop().browse(new URI(url));
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
			}
		}
	}
}
