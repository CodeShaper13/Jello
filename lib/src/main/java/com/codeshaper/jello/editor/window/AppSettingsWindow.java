package com.codeshaper.jello.editor.window;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.joml.Math;

import com.codeshaper.jello.editor.GuiLayoutBuilder;
import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.engine.ApplicationSettings;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.asset.Texture;
import com.codeshaper.jello.engine.database.AssetDatabase;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class AppSettingsWindow extends EditorWindow {

	private final File settingsFile;
	private ApplicationSettings appSettings;
	private JPanel builderPanel;

	public AppSettingsWindow() {
		super("Game Settings", "applicationSettings");

		this.settingsFile = new File(JelloEditor.instance.rootProjectFolder.toFile(), "appSettings.json");
		
		this.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		this.setLayout(new BorderLayout());

		JButton saveBtn = new JButton("Save");
		saveBtn.addActionListener((e) -> {
			this.saveAppSettings(this.appSettings);
		});
		this.add(saveBtn, BorderLayout.SOUTH);
		
		// Run later so the LWJGL context can be made.
		SwingUtilities.invokeLater(() -> {
			this.appSettings = this.loadAppSettings();
			this.makeControls();			
		});
	}
	
	@Override
	public boolean getHasMoreOptions() {
		return true;
	}
	
	@Override
	public void addMoreOptions(JPopupMenu menu) {
		JMenuItem reset = new JMenuItem("Reset");
		reset.addActionListener(e -> {
			this.appSettings = new ApplicationSettings();
			this.remove(this.builderPanel);
			this.makeControls();
		});
		menu.add(reset);
	}


	/**
	 * Reads the appSettings.json file and returns it to disk. If the file doesn't
	 * exist, or there was an error, a new ApplicationSettings instance is returned
	 * with the default values.
	 * 
	 * @return
	 */
	private ApplicationSettings loadAppSettings() {
		ApplicationSettings settings = null;
		
		if(this.settingsFile.exists()) {
			try {
				settings = AssetDatabase.getInstance().serializer.deserialize(
						this.settingsFile,
						ApplicationSettings.class);
			} catch (JsonIOException | JsonSyntaxException | IOException e) {
				Debug.logError("Error reading appSettings.json");

			}
			if(settings == null) {
				Debug.logError("Error reading appSettings.json");
			}
		}
		
		return settings != null ? settings : new ApplicationSettings();
	}

	
	/**
	 * Writes an {@link ApplicationSettings} object to disk.
	 * 
	 * @param settings the {@link ApplicationSettings} to save.
	 */
	private void saveAppSettings(ApplicationSettings settings) {
		boolean success;
		try {
			success = AssetDatabase.getInstance().serializer.serializeToFile(settings, this.settingsFile);
		} catch (Exception e) {
			success = false;
		}
		
		if(!success) {
			Debug.logError("Error saving appSettings.json");
		}
	}

	private void makeControls() {
		GuiLayoutBuilder builder = new GuiLayoutBuilder();
		
		ApplicationSettings settings = this.appSettings;

		builder.textField("Window Title", settings.windowTitle, v -> { settings.windowTitle = v; });
		builder.assetField("Window Icon", settings.windowIcon, Texture.class, v -> { settings.windowIcon = v; });
		builder.checkbox("Fullscreen", settings.fullscreen, (v) -> { settings.fullscreen = v; });
		builder.checkbox("Resizeable", settings.isResizeable, (v) -> { settings.isResizeable = v; });
		builder.vector2iField("WindowSize", settings.windowSize, v -> {
			settings.windowSize.setComponent(0, Math.max(0, v.x));
			settings.windowSize.setComponent(1, Math.max(0, v.y));
		});
		builder.space();
		builder.checkbox("Use VSync", settings.useVSync, v -> { settings.useVSync = v; });
		builder.space();
		builder.intField("Target Frames Per Second", settings.targetFps, v -> { settings.targetFps = v; } );
		builder.intField("Target Updates Per Second", settings.targetUps, v -> { settings.targetUps = v; } );
		builder.space(); // Don't let the last field touch the button if the window is small.
		
		this.builderPanel = builder.getPanel();
		this.add(this.builderPanel, BorderLayout.NORTH);
	}
}
