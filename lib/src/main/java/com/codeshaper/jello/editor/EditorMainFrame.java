package com.codeshaper.jello.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import com.codeshaper.jello.editor.inspector.ComponentEditor;
import com.codeshaper.jello.editor.menu.EditorMenuBar;
import com.codeshaper.jello.editor.render.SceneViewPanel;
import com.codeshaper.jello.editor.window.AppSettingsWindow;
import com.codeshaper.jello.editor.window.AssetDatabaseViewer;
import com.codeshaper.jello.editor.window.ConsoleWindow;
import com.codeshaper.jello.editor.window.EditorSettingsWindow;
import com.codeshaper.jello.editor.window.FileBrowserWindow;
import com.codeshaper.jello.editor.window.HierarchyWindow;
import com.codeshaper.jello.editor.window.InspectorWindow;
import com.codeshaper.jello.engine.audio.SoundManager;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import ModernDocking.DockingRegion;
import ModernDocking.app.AppState;
import ModernDocking.app.Docking;
import ModernDocking.app.DockingState;
import ModernDocking.app.RootDockingPanel;
import ModernDocking.app.WindowLayoutBuilder;
import ModernDocking.exception.DockingLayoutException;
import ModernDocking.layouts.ApplicationLayout;
import ModernDocking.layouts.DockingLayouts;
import ModernDocking.layouts.WindowLayout;

public class EditorMainFrame extends JFrame {

	public final SceneViewPanel sceneView;

	// Fields to hold all of the builtin windows.
	public final InspectorWindow inspector;
	public final ConsoleWindow console;
	public final HierarchyWindow hierarchy;
	public final FileBrowserWindow fileBrowser;
	
	private boolean isDarkMode;

	public EditorMainFrame(JelloEditor editor) {
		super("Jello");

		this.setDarkMode(JelloEditor.instance.properties.getBoolean("isDarkMode", true));

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(1000, 800));
		ImageIcon icon = new ImageIcon(EditorMainFrame.class.getResource("/jelloIcon.png"));
		this.setIconImage(icon.getImage());

		Docking.initialize(this);
		AppState.setPersistFile(new File(editor.rootProjectFolder.toFile(), "layout.xml"));

		this.sceneView = new SceneViewPanel();

		this.inspector = new InspectorWindow();
		this.console = new ConsoleWindow();
		this.hierarchy = new HierarchyWindow();
		this.fileBrowser = new FileBrowserWindow();
		new EditorSettingsWindow();
		new AssetDatabaseViewer();
		new AppSettingsWindow();

		RootDockingPanel root = new RootDockingPanel(this);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, root, this.sceneView);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				splitPane.setDividerLocation(0.75);
			}
		});
		splitPane.setResizeWeight(0.5);
		// splitPane.setDividerLocation(0.75);
		this.add(splitPane, BorderLayout.CENTER);

		// Create the default dockable layout.
		WindowLayout defaultLayout = this.getDefaultLayout();
		DockingState.restoreWindowLayout(this, defaultLayout);
		DockingLayouts.addLayout("Default", new ApplicationLayout(defaultLayout));

		this.setJMenuBar(new EditorMenuBar(this));
		this.pack();
		this.setVisible(true);
		this.transferFocus();

		SwingUtilities.invokeLater(() -> {
			try {
				AppState.restore();
			} catch (DockingLayoutException e) {
				e.printStackTrace();
			}
			AppState.setAutoPersist(true);
		});
		
		this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                SoundManager.shutdown();
            }
        });
	}
	
	public boolean isDarkMode() {
		return this.isDarkMode;
	}
	
	public void setDarkMode(boolean darkMode) {
		if(darkMode) {
			FlatDarkLaf.setup();
		} else {
			FlatLightLaf.setup();
		}
		
		SwingUtilities.updateComponentTreeUI(this);
		this.isDarkMode = darkMode;
		JelloEditor.instance.properties.setBoolean("isDarkMode", darkMode);
	}

	private WindowLayout getDefaultLayout() {
		WindowLayoutBuilder layoutBuilder = new WindowLayoutBuilder("hierarchy");
		layoutBuilder.dock("fileViewer", "hierarchy", DockingRegion.SOUTH);
		layoutBuilder.dock("inspector", "hierarchy", DockingRegion.WEST);
		layoutBuilder.dock("console", "fileViewer", DockingRegion.EAST);

		return layoutBuilder.build();
	}
}
