package com.codeshaper.jello.editor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EventListener;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import org.apache.commons.io.FilenameUtils;
import org.lwjgl.glfw.GLFWErrorCallback;

import com.codeshaper.jello.editor.event.PlayModeListener;
import com.codeshaper.jello.editor.event.PlayModeListener.State;
import com.codeshaper.jello.editor.event.ProjectReloadListener;
import com.codeshaper.jello.editor.event.ProjectSaveListener;
import com.codeshaper.jello.editor.event.SceneChangeListener;
import com.codeshaper.jello.engine.Application;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.Scene;
import com.codeshaper.jello.engine.asset.SerializedJelloObject;
import com.codeshaper.jello.engine.audio.AudioListener;
import com.codeshaper.jello.engine.audio.SoundManager;
import com.codeshaper.jello.engine.lighting.DirectionalLight;
import com.codeshaper.jello.engine.logging.ILogHandler;
import com.codeshaper.jello.engine.rendering.Camera;
import com.codeshaper.jello.engine.rendering.GameRenderer;
import com.codeshaper.jello.engine.rendering.MeshRenderer;

import ModernDocking.Dockable;
import ModernDocking.app.Docking;

public class JelloEditor {

	public static final String EDITOR_VERSION = "0.0";
	public static final String REPORT_ISSUE_URL = "http://github.com/CodeShaper13/Jello/issues";
	public static final String DOCUMENTAION_URL = "http://github.com/CodeShaper13/Jello/wiki";

	public static JelloEditor instance;

	public static void main(String[] args) {
		if (args.length == 0) {
			args = new String[] { "D:\\Jello\\Projects\\devProject" };
		}

		if (args.length > 0) {
			// A path was supplied.
			String path = args[0];
			SwingUtilities.invokeLater(() -> {
				new JelloEditor(Paths.get(path));
			});
		} else {
			System.err.println(
					"Jello: No project path specified.  Pass a path in pointing to the root folder of the project.  Example: \"C:/JelloProjects/MyProject\"");
		}
	}

	/**
	 * Gets a Dockable window of the passed type. Class comparison is used, not
	 * isinstance, so only direct matches will be returned. If there are no
	 * registered windows of the type, null is returned.
	 * 
	 * @param <T>
	 * @param type
	 * @return
	 */
	public static <T extends Dockable> T getWindow(Class<T> type) {
		for (Dockable dockable : Docking.getDockables()) {
			if (dockable.getClass() == type) {
				return type.cast(dockable);
			}
		}

		return null;
	}

	public final Path rootProjectFolder;
	public final Path assetsFolder;
	public final EditorAssetDatabase assetDatabase;
	public final EditorMainFrame window;
	public final ILogHandler logHandler;
	public final GameRenderer renderer;
	public final EditorProperties properties;
	public final EditorSceneManager sceneManager;

	private final EventListenerList listenerList;
	/**
	 * The running instance of the application. Null if no instance is running.
	 */
	private Application application;

	private JelloEditor(Path projectFolder) {
		JelloEditor.instance = this;

		GLFWErrorCallback.createPrint().set();

		boolean isInitialLoad = false;

		this.rootProjectFolder = projectFolder;

		// Create the /assets folder if it doesn't exist.
		this.assetsFolder = this.rootProjectFolder.resolve("assets");
		if (!Files.exists(this.assetsFolder)) {
			isInitialLoad = true;
			try {
				Files.createDirectories(this.assetsFolder);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.writeEditorVersionFile();

		SoundManager.initialize();

		this.properties = new EditorProperties(new File(this.rootProjectFolder.toFile(), "editor.properties"));
		this.listenerList = new EventListenerList();
		this.assetDatabase = new EditorAssetDatabase(this.assetsFolder);
		GuiBuilder.init(this);

		this.sceneManager = new EditorSceneManager(this);

		this.assetDatabase.rebuild();

		this.window = new EditorMainFrame(this);
		this.logHandler = this.window.console;

		this.window.sceneView.createContext();
		
		this.renderer = new GameRenderer();

		if (isInitialLoad) {
			Path path = Path.of("scene." + SerializedJelloObject.EXTENSION);
			if (!this.assetDatabase.exists(path)) {
				Scene scene = this.constructDefaultScene(path);
				this.sceneManager.loadScene(scene);
			}
		} else {
			this.sceneManager.readOpenScenesFromPreferences();
		}
	}

	/**
	 * Saves the project. This saves all open Scenes and and Editor preferences, and
	 * raise the {@link ProjectSaveListener} event. Saving is prohibited in play
	 * mode. If this is called during play mode, nothing happens.
	 */
	public void saveProject() {
		if (this.isInPlayMode()) {
			return; // Can't save in Play mode.
		}

		this.sceneManager.saveAllScenes();
		this.sceneManager.writeOpenScenesToPreferences();

		this.raiseEvent(ProjectSaveListener.class, (listener) -> {
			listener.onSave();
		});

		this.properties.save();
	}

	public void preformUndo() {
		Debug.logWarning("Undo is not yet implemented."); // TODO implement undo.
	}

	public void preformRedo() {
		Debug.logWarning("Redo is not yet implemented."); // TODO implement redo.
	}

	public void reloadProject() {
		this.assetDatabase.rebuild();
	}

	/**
	 * Starts play mode. If play mode has already been started, nothing happens.
	 * 
	 * @return {@link true} if play mode started, {@link false} if it could not be
	 *         for any reason.
	 */
	public boolean startPlaymode() {
		if (this.isInPlayMode()) {
			return false;
		}

		this.raiseEvent(PlayModeListener.class, (listener) -> {
			listener.onPlaymodeChange(State.STARTED);
		});

		SceneManagerSnapshot snapshot = new SceneManagerSnapshot(this.sceneManager);

		this.application = new Application(this.sceneManager, () -> {
			this.application = null;
			
			snapshot.restore(this.sceneManager);
			
			this.raiseEvent(PlayModeListener.class, (listener) -> {
				listener.onPlaymodeChange(State.STOPPED);
			});
		});
		this.application.start(); // TODO start in the correct scene.

		return true;
	}

	/**
	 * Stops play mode. The Application does not stop immediately, it will terminate
	 * on it's next frame. If play mode has not been started, nothing happens.
	 */
	public void stopPlaymode() {
		if (!this.isInPlayMode()) {
			return;
		}

		this.application.stop();
	}

	/**
	 * Checks if the Editor is in play mode.
	 * 
	 * @return {@link true} if the Editor is in play mode, {@link false} if it is
	 *         not.
	 */
	public boolean isInPlayMode() {
		return this.application != null;
	}

	public <T extends EventListener> void raiseEvent(Class<T> clazz, Consumer<T> c) {
		for (T listener : this.listenerList.getListeners(clazz)) {
			c.accept(listener);
		}
	}

	////////////////////////////////////////
	// Event Listener add/remove methods. //
	////////////////////////////////////////

	public void addPlayModeListener(PlayModeListener l) {
		this.listenerList.add(PlayModeListener.class, l);
	}

	public void removePlayModeListener(PlayModeListener l) {
		this.listenerList.remove(PlayModeListener.class, l);
	}

	public void addProjectReloadListener(ProjectReloadListener l) {
		this.listenerList.add(ProjectReloadListener.class, l);
	}

	public void removeProjectReloadListener(ProjectReloadListener l) {
		this.listenerList.remove(ProjectReloadListener.class, l);
	}

	public void addProjectSaveListener(ProjectSaveListener l) {
		this.listenerList.add(ProjectSaveListener.class, l);
	}

	public void removeProjectSaveListener(ProjectSaveListener l) {
		this.listenerList.remove(ProjectSaveListener.class, l);
	}

	public void addSceneChangeListener(SceneChangeListener l) {
		this.listenerList.add(SceneChangeListener.class, l);
	}

	public void removeSceneChangeListener(SceneChangeListener l) {
		this.listenerList.remove(SceneChangeListener.class, l);
	}

	////////////////////////////////////////

	/**
	 * 
	 * @return {@code true}
	 */
	private Scene constructDefaultScene(Path path) {
		String assetName = FilenameUtils.removeExtension(path.getFileName().toString());
		System.out.println(path.getParent());
		Scene scene = (Scene) this.assetDatabase.createAsset(Scene.class, path.getParent(),
				assetName);

		GameObject cameraObj = new GameObject("Main Camera", scene);
		cameraObj.setLocalPosition(0, 5, -10);
		cameraObj.setLocalEulerAngles(20, 0, 0);
		cameraObj.addComponent(Camera.class);
		cameraObj.addComponent(AudioListener.class);

		GameObject meshObj = new GameObject("Cube", scene);
		meshObj.addComponent(MeshRenderer.class);

		GameObject lightObj = new GameObject("Light", scene);
		lightObj.setLocalPosition(0, 0, -2);
		lightObj.setLocalEulerAngles(20, 45, 0);
		lightObj.addComponent(DirectionalLight.class);

		this.assetDatabase.saveAsset(scene);

		return scene;
	}

	/**
	 * Creates a version file that contains the version of the Editor.
	 */
	private void writeEditorVersionFile() {
		File file = new File(this.rootProjectFolder.toFile(), "version.txt");
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			writer.write(EDITOR_VERSION);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
