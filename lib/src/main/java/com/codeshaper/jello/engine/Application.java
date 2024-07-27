package com.codeshaper.jello.engine;

import org.joml.Math;
import org.joml.Matrix4f;

import com.codeshaper.jello.engine.component.Camera;
import com.codeshaper.jello.engine.component.JelloComponent;
import com.codeshaper.jello.engine.component.MeshRenderer;
import com.codeshaper.jello.engine.rendering.GameRenderer;

public class Application {

	private AppSettings appSettings;
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
		this.appSettings = new AppSettings();
		this.window = new Window(this.appSettings, () -> {
			this.resize();
			return null;
		});
		this.renderer = new GameRenderer();

		Input.initialize(this.window.getWindowHandle());
	}

	/**
	 * Starts the Application if it is not running already.
	 */
	public void start() {
		if (this.running) {
			System.err.println("Application can not be started, it is already running!");
			return;
		}

		this.sceneManager = new SceneManager();

		// Jello
		Scene scene = new Scene(null); // TODO
		GameObject obj = new GameObject("GameObj1", scene);
		Camera camera = obj.addComponent(Camera.class);
		camera.onStart();
		obj.addComponent(MeshRenderer.class);
		this.sceneManager.loadScene(scene);

		this.running = true;
		this.run();
	}

	/**
	 * Stops the Application.
	 */
	public void stop() {
		this.running = false;
	}

	public float getVolume() {
		return 0f; // TODO
	}

	public void setVolume(float volume) {
		volume = Math.clamp(0f, 1f, volume);
		// TODO
	}

	public void setMouseState() {

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
		
		if(this.onClose != null) {
			this.onClose.run();
		}
	}
}