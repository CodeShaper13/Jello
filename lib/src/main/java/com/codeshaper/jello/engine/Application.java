package com.codeshaper.jello.engine;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.List;

import org.joml.Math;
import org.joml.Matrix4f;

import com.codeshaper.jello.engine.component.Camera;
import com.codeshaper.jello.engine.component.JelloComponent;
import com.codeshaper.jello.engine.database.AssetDatabase;
import com.codeshaper.jello.engine.rendering.GameRenderer;
import com.google.gson.Gson;

public class Application {

	private ApplicationSettings appSettings;
	private Window window;
	private GameRenderer renderer;
	private SceneManager sceneManager;
	private boolean running;

	/**
	 * If not null, it is invoked when the Application stops. Used by the Editor to
	 * know when the Application closes.
	 */
	public Runnable onClose;

	public static void main(String[] args) {
		new Application().start();
	}

	public Application() {
		this(null);
	}

	public Application(SceneManager sceneManager) {
		if (AssetDatabase.getInstance() == null) {
			new AssetDatabase(null); // TODO what should this be in a build?
		}
		
		this.appSettings = this.loadAppSettings();

		this.window = new Window(this.appSettings, () -> {
			this.resize();
			return null;
		});

		if (sceneManager != null) {
			// Use the Scene Manager that was supplied by the Editor.
			this.sceneManager = sceneManager;
		} else {
			this.sceneManager = new SceneManager();
			Scene startingScene = this.appSettings.startingScene;
			if (startingScene != null) {
				this.sceneManager.loadScene(startingScene);
			} else {
				// No starting scene set.
				AssetDatabase database = AssetDatabase.getInstance();
				List<Path> paths = database.getAllAssetsOfType(Scene.class, true);
				if (paths.size() >= 1) {
					this.sceneManager.loadScene((Scene) database.getAsset(paths.get(0)));
				}
			}
		}

		this.renderer = new GameRenderer();

		Input.initialize(this.window.getWindowHandle());
	}

	/**
	 * Starts the Application. If it is already running nothings happens and an
	 * error is logged..
	 */
	public void start() {
		if (this.running) {
			System.err.println("Application can not be started, it is already running!");
			return;
		}

		this.running = true;
		this.run();
	}

	/**
	 * Shuts down the Application.
	 */
	public void stop() {
		this.running = false;
	}

	public SceneManager getSceneManager() {
		return this.sceneManager;
	}
	
	public float getVolume() {
		return 0f; // TODO
	}

	public void setVolume(float volume) {
		volume = Math.clamp(0f, 1f, volume);
		// TODO
	}

	private void resize() {
		// Nothing to be done yet
	}

	private void run() {
		long initialTime = System.currentTimeMillis();
		float timeU = 1000.0f / this.appSettings.targetUps;
		float timeR = this.appSettings.targetFps > 0 ? 1000.0f / this.appSettings.targetFps : 0;
		float deltaUpdate = 0;
		float deltaFps = 0;

		long updateTime = initialTime;
		while (running && !window.windowShouldClose()) {
			window.pollEvents();

			long now = System.currentTimeMillis();
			deltaUpdate += (now - initialTime) / timeU;
			deltaFps += (now - initialTime) / timeR;

			if (this.appSettings.targetFps <= 0 || deltaFps >= 1) {
				// appLogic.input(window, scene, now - initialTime);
			}

			if (deltaUpdate >= 1) {
				long diffTimeMillis = now - updateTime;
				for (Scene scene : this.sceneManager.getScenes()) {
					for (GameObject obj : scene.getRootGameObjects()) {
						for (JelloComponent component : obj.getAllComponents()) {
							component.onUpdate(diffTimeMillis);
						}
					}
				}
				updateTime = now;
				deltaUpdate--;
			}

			if (this.appSettings.targetFps <= 0 || deltaFps >= 1) {
				for (Camera camera : Camera.getAllCameras()) {
					// TODO sort with Camera#depth
					if (camera.isEnabled()) {
						this.renderer.render(this.sceneManager, camera, new Matrix4f(), this.window.getWidth(),
								this.window.getHeight());
					}
				}

				deltaFps--;
				window.update();
			}

			Input.onEndOfFrame();

			initialTime = now;
		}

		this.cleanup();
	}

	private void cleanup() {
		// TODO
		this.window.cleanup();

		if (this.onClose != null) {
			this.onClose.run();
		}
	}

	private ApplicationSettings loadAppSettings() {
		Gson gson = AssetDatabase.getInstance().createGsonBuilder().create();
		try (FileReader reader = new FileReader(new File("appSettings.json"))) {
			return gson.fromJson(reader, ApplicationSettings.class);
		} catch (Exception e) {
			e.printStackTrace();
			return new ApplicationSettings(); // Settings are missing?
		}
	}
}