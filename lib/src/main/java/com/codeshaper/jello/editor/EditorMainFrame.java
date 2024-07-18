package com.codeshaper.jello.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import com.codeshaper.jello.editor.window.AssetDatabaseViewer;
import com.codeshaper.jello.editor.window.ConsoleWindow;
import com.codeshaper.jello.editor.window.FileBrowserWindow;
import com.codeshaper.jello.editor.window.HierarchyWindow;
import com.codeshaper.jello.editor.window.InspectorWindow;

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

	public final SceneView sceneView;

	// Fields to hold all of the builtin windows.
	public final InspectorWindow inspector;
	public final ConsoleWindow console;
	public final HierarchyWindow hierarchy;
	public final FileBrowserWindow fileBrowser;

	public EditorMainFrame(JelloEditor editor) {
		super("Jello");

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(1000, 800));

		Docking.initialize(this);
		AppState.setPersistFile(new File(editor.rootProjectFolder.toFile(), "layout.xml"));
		
		this.sceneView = new SceneView();

		this.inspector = new InspectorWindow();
		this.console = new ConsoleWindow();
		this.hierarchy = new HierarchyWindow();
		this.fileBrowser = new FileBrowserWindow();
		new AssetDatabaseViewer();

		RootDockingPanel root = new RootDockingPanel(this);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, root, this.sceneView);
		SwingUtilities.invokeLater(new Runnable() {			
			@Override
			public void run() {
				splitPane.setDividerLocation(0.75);				
			}
		});
		splitPane.setResizeWeight(0.5);
		//splitPane.setDividerLocation(0.75);
		this.add(splitPane, BorderLayout.CENTER);

		// Create the default dockable layout.
		WindowLayout defaultLayout = this.getDefaultLayout();
		DockingState.restoreWindowLayout(this, defaultLayout);
		DockingLayouts.addLayout("Default", new ApplicationLayout(defaultLayout));
		
		this.setJMenuBar(new EditorMenuBar(this, JelloEditor.REPORT_ISSUE_URL, JelloEditor.DOCUMENTAION_URL));
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
	}

	private WindowLayout getDefaultLayout() {
		WindowLayoutBuilder layoutBuilder = new WindowLayoutBuilder("hierarchy");
		layoutBuilder.dock("fileViewer", "hierarchy", DockingRegion.SOUTH);
		layoutBuilder.dock("inspector", "hierarchy", DockingRegion.WEST);
		layoutBuilder.dock("console", "fileViewer", DockingRegion.EAST);

		return layoutBuilder.build();
	}
}
