package com.codeshaper.jello.editor.window;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import javax.swing.JButton;
import javax.swing.JScrollPane;

import com.codeshaper.jello.editor.GuiLayoutBuilder;
import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.engine.ApplicationSettings;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.database.AssetDatabase;
import com.google.gson.Gson;

public class AppSettingsWindow extends EditorWindow {

	private final File settingsFile;
	private final ApplicationSettings appSettings;

	public AppSettingsWindow() {
		super("Game Settings", "applicationSettings");

		this.settingsFile = new File(JelloEditor.instance.rootProjectFolder.toFile(), "appSettings.json");
		this.appSettings = this.loadAppSettings();

		GuiLayoutBuilder builder = new GuiLayoutBuilder();

		this.makeControls(builder);

		this.setLayout(new BorderLayout());
		JScrollPane pane = new JScrollPane(builder.getPanel());
		this.add(pane, BorderLayout.CENTER);

		JButton saveBtn = new JButton("Save");
		saveBtn.addActionListener((e) -> {
			this.saveAppSettings(this.appSettings);
		});
		this.add(saveBtn, BorderLayout.SOUTH);
	}

	/**
	 * Reads the appSettings.json file and returns it to disk. If the file doesn't
	 * exist, or there was an error, a new ApplicationSettings instance is returned
	 * with the default values.
	 * 
	 * @return
	 */
	private ApplicationSettings loadAppSettings() {
		Gson gson = AssetDatabase.getInstance().createGsonBuilder().create();
		
		if(this.settingsFile.exists()) {
			try (FileReader reader = new FileReader(this.settingsFile)) {
				return gson.fromJson(reader, ApplicationSettings.class);
			} catch (Exception e) {
				Debug.logError("Error reading Application Settings");
				e.printStackTrace();
			}
		}
		
		return new ApplicationSettings();
	}

	/**
	 * Writes an {@link ApplicationSettings} object to disk.
	 * 
	 * @param settings the {@link ApplicationSettings} to save.
	 */
	private void saveAppSettings(ApplicationSettings settings) {
		Gson gson = AssetDatabase.getInstance().createGsonBuilder().create();
		try (FileWriter writer = new FileWriter(this.settingsFile)) {
			gson.toJson(settings, writer);
		} catch (Exception e) {
			Debug.logError("Error saving Application Settings.");
			e.printStackTrace();
		}
	}

	private void makeControls(GuiLayoutBuilder builder) {
		ApplicationSettings settings = this.appSettings;

		// TODO
	}
}
