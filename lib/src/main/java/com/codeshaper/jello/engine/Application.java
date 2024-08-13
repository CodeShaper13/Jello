package com.codeshaper.jello.engine;

import static org.lwjgl.glfw.GLFW.*;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.opengl.WGL;
import org.lwjgl.system.windows.User32;

import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.engine.audio.SoundManager;
import com.codeshaper.jello.engine.database.AssetDatabase;
import com.codeshaper.jello.engine.rendering.Camera;
import com.codeshaper.jello.engine.rendering.GameRenderer;
import com.google.gson.Gson;

public class Application {

	private static Application instance;

	public final Window window;
	public final SceneManager sceneManager;

	private ApplicationSettings appSettings;
	private GameRenderer renderer;
	private boolean running;

	/**
	 * If not null, it is invoked when the Application stops. Used by the Editor to
	 * know when the Application closes.
	 */
	private Runnable onClose;

	public static void main(String[] args) {
		new Application(null, null).start();
	}

	/**
	 * Gets the instance of the Application. In builds, this will never return
	 * {@code null}. In the Editor, it will be {@code null} unless the Editor is in
	 * Play Mode.
	 * 
	 * @return the instance of the running Application.
	 */
	public static Application getInstance() {
		return Application.instance;
	}

	/**
	 * Checks if the Application is running. In builds this will always return
	 * {@code true}, and in the Editor it will return {@code true} only if the
	 * Editor is in Play Mode.
	 * 
	 * @return
	 */
	public static boolean isPlaying() {
		return Application.instance != null;
	}

	/**
	 * Checks if the Editor is running.
	 * 
	 * @return {@code true} if the Editor is running.
	 */
	public static boolean isInEditor() {
		return JelloEditor.instance != null;
	}

	// Called from Editor.
	public Application(SceneManager sceneManager, Runnable onClose) {
		if (Application.instance != null) {
			throw new Error("Multiple Application instances can not be created!");
		}

		Application.instance = this;
		this.onClose = onClose;

		this.appSettings = new ApplicationSettings(); // this.loadAppSettings();

		this.window = new Window(this.appSettings);

		if (AssetDatabase.getInstance() == null) { // null in builds.
			Path projectFolder = Path.of("C:\\Users\\Pj\\Desktop\\jelloprojects\\dev\\assets"); // TODO what should this
																								// be in a
			// build?
			new AssetDatabase(projectFolder);
		}

		if (!SoundManager.isInitialized()) {
			SoundManager.initialize();
		}

		this.renderer = new GameRenderer();

		if (sceneManager == null) { // null in builds.
			this.sceneManager = new SceneManager();
			Scene startingScene = this.appSettings.startingScene;
			if (startingScene != null) {
				this.sceneManager.loadScene(startingScene);
			} else {
				// No starting scene set. We're assuming the user forgot to set a starting scene
				// because there's only one in their project. Search the Asset Database for all
				// Scenes and load the first one we find.
				AssetDatabase database = AssetDatabase.getInstance();
				List<Path> paths = database.getAllAssetsOfType(Scene.class, true);
				if (paths.size() >= 1) {
					this.sceneManager.loadScene((Scene) database.getAsset(paths.get(0)));
				}
			}
		} else {
			this.sceneManager = sceneManager;
			
			for(int i = sceneManager.getLoadedSceneCount() - 1; i >= 0; i--) {
				Scene scene = sceneManager.getLoadedScene(i);
				for (int j = scene.getRootGameObjectCount() - 1; j >= 0; j--) {
					sceneManager.recursiveCallOnConstruct(scene.getRootGameObject(j));
				}
			}
			
			/*
			 * if (launchArgs.startingScenes.size() > 0) { for (Path path :
			 * launchArgs.startingScenes) { Asset asset =
			 * AssetDatabase.getInstance().getAsset(path); if (asset instanceof Scene) {
			 * this.sceneManager.loadScene((Scene) asset); } } } else { Scene startingScene
			 * = this.appSettings.startingScene; if (startingScene != null) {
			 * this.sceneManager.loadScene(startingScene); } else { // No starting scene
			 * set. AssetDatabase database = AssetDatabase.getInstance(); List<Path> paths =
			 * database.getAllAssetsOfType(Scene.class, true); if (paths.size() >= 1) {
			 * this.sceneManager.loadScene((Scene) database.getAsset(paths.get(0))); } } }
			 */
		}

		Input.initialize(this.window.windowHandle);
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

		if (this.isEditor()) {
			SwingUtilities.invokeLater(new EditorLoop());
		} else {
			new BuildLoop().run();
		}
	}

	/**
	 * Stops down the Application. This does not immediately stop the Application.
	 * Another frame may still be run.
	 */
	public void stop() {
		this.running = false;
	}

	/**
	 * Checks if the Application is running along side the Editor.
	 * 
	 * @return {@code true} if the Application was launched from the Editor,
	 *         {@code false} if it was launched as a build.
	 */
	public boolean isEditor() {
		return JelloEditor.instance != null;
	}

	private void cleanup() {
		this.window.cleanup();

		if (!isEditor() && SoundManager.isInitialized()) {
			SoundManager.shutdown();
		}

		if (this.onClose != null) {
			this.onClose.run();
		}

		Application.instance = null;
	}

	private ApplicationSettings loadAppSettings() {
		Gson gson = AssetDatabase.getInstance().createGsonBuilder().create();
		File file = new File("appSettings.json");
		if (file.exists()) {
			try (FileReader reader = new FileReader(file)) {
				return gson.fromJson(reader, ApplicationSettings.class);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new ApplicationSettings(); // Settings are missing?
	}

	private abstract class Loop implements Runnable {

		protected long initialTime;

		protected float timeU;
		protected float timeR;
		protected float deltaUpdate;
		protected float deltaFps;
		protected long updateTime;

		public Loop() {
			this.initialTime = System.currentTimeMillis();

			this.timeU = 1000.0f / appSettings.targetUps;
			this.timeR = appSettings.targetFps > 0 ? 1000.0f / appSettings.targetFps : 0;
			this.deltaUpdate = 0;
			this.deltaFps = 0;
			this.updateTime = initialTime;
		}

		public void preformLoopIteration() {
			glfwPollEvents();

			long now = System.currentTimeMillis();
			deltaUpdate += (now - initialTime) / timeU;
			deltaFps += (now - initialTime) / timeR;

			if (appSettings.targetFps <= 0 || deltaFps >= 1) {
				// appLogic.input(window, scene, now - initialTime);
			}

			if (deltaUpdate >= 1) {
				long diffTimeMillis = now - updateTime;

				// Call onStart on every component this is enabled in the scene that hasn't
				// gotten onStart called on them yet.
				for (Scene scene : sceneManager.getScenes()) {
					for (GameObject rootObject : scene.getRootGameObjects()) {
						rootObject.invokeRecursively(rootObject, (c) -> {
							if (c.isEnabledInScene() && !c.hasOnStartBeenCalled) {
								c.hasOnStartBeenCalled = true;
								c.invokeOnStart();
							}
						});
					}
				}

				// Call onUpdate on every component that is enabled in the scene.
				for (Scene scene : sceneManager.getScenes()) {
					for (GameObject rootObject : scene.getRootGameObjects()) {
						rootObject.invokeRecursively(rootObject, (c) -> {
							if (c.isEnabledInScene()) {
								c.onUpdate(diffTimeMillis);
							}
						});
					}
				}
				updateTime = now;
				deltaUpdate--;
			}

			if (appSettings.targetFps <= 0 || deltaFps >= 1) {
				for (Camera camera : Camera.getAllCameras()) {
					// TODO sort with Camera#depth
					if (camera.isEnabled()) {
						Matrix4f m = camera.getOwner().getLocalMatrix();
						Vector3f v = new Vector3f();
						m.getTranslation(v);
						v.mul(-1f);
						m.setTranslation(v);
						renderer.render(
								sceneManager,
								camera,
								m,
								window.getWidth(),
								window.getHeight());
					}
				}

				deltaFps--;
				glfwSwapBuffers(window.windowHandle);
			}

			Input.onEndOfFrame();

			initialTime = now;
		}
	}

	private class EditorLoop extends Loop implements Runnable {

		@Override
		public void run() {
			if (!running || glfwWindowShouldClose(window.windowHandle)) {
				cleanup();
				return;
			}

			long hwnd = GLFWNativeWin32.glfwGetWin32Window(window.windowHandle);
			long hdc = User32.GetDC(hwnd);
			WGL.wglMakeCurrent(hdc, JelloEditor.instance.window.sceneView.getContext());

			this.preformLoopIteration();

			SwingUtilities.invokeLater(this);
		}

	}

	private class BuildLoop extends Loop {

		@Override
		public void run() {
			while (running && !glfwWindowShouldClose(window.windowHandle)) {
				this.preformLoopIteration();
			}

			cleanup();
		}
	}

	public static class LaunchArguments {

		public final List<Path> startingScenes;

		public LaunchArguments() {
			this.startingScenes = new ArrayList<Path>();
		}

		public LaunchArguments addStartingScene(Scene scene) {
			this.startingScenes.add(scene.location.getPath());
			return this;
		}
	}
}