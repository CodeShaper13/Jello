package com.codeshaper.jello.editor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import org.lwjgl.glfw.GLFWErrorCallback;

import com.codeshaper.jello.editor.SceneView.SceneAWTGLCanvas;
import com.codeshaper.jello.editor.event.PlayModeListener;
import com.codeshaper.jello.editor.event.PlayModeListener.State;
import com.codeshaper.jello.editor.event.ProjectReloadListener;
import com.codeshaper.jello.editor.event.ProjectReloadListener.Phase;
import com.codeshaper.jello.editor.event.ProjectSaveListener;
import com.codeshaper.jello.editor.event.SceneChangeListener;
import com.codeshaper.jello.editor.property.drawer.FieldDrawerRegistry;
import com.codeshaper.jello.engine.Application;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.ISceneProvider;
import com.codeshaper.jello.engine.asset.Scene;
import com.codeshaper.jello.engine.component.*;
import com.codeshaper.jello.engine.logging.ILogHandler;
import com.codeshaper.jello.engine.render.Renderer;

import ModernDocking.Dockable;
import ModernDocking.app.Docking;

public class JelloEditor implements ISceneProvider {

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
	public final ComponentList componentList;
	public final FieldDrawerRegistry filedDrawers;
	public final EditorMainFrame window;
	public final ILogHandler logHandler;
	public final Renderer renderer;
	public final EditorProperties properties;

	/**
	 * This list always has a length of 1, the current scene. A list so it can be
	 * used by {@link ISceneProvider}.
	 */
	private final List<Scene> loadedScene;
	private final EventListenerList listenerList;
	/**
	 * The running instance of the application. Null if no instance is running.
	 */
	private Application application;

	private JelloEditor(Path projectFolder) {
		instance = this;

		// Create the /assets folder if it doesn't exist.
		this.rootProjectFolder = projectFolder;
		this.assetsFolder = this.rootProjectFolder.resolve("assets");
		if (!Files.exists(this.assetsFolder)) {
			try {
				Files.createDirectories(this.assetsFolder);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.writeEditorVersionFile();
		
		this.properties = new EditorProperties(new File(this.rootProjectFolder.toFile(), "editor.properties"));
		
		this.listenerList = new EventListenerList();

		this.assetDatabase = new EditorAssetDatabase(this.assetsFolder);
		this.assetDatabase.buildDatabase();

		this.componentList = new ComponentList();

		this.filedDrawers = new FieldDrawerRegistry();
		this.filedDrawers.registerBuiltinDrawers();

		this.window = new EditorMainFrame(this);

		this.logHandler = this.window.console;

		this.renderer = this.window.sceneView.createContext();

		GLFWErrorCallback.createPrint().set();

		this.enableEditorContext();

		this.loadedScene = new ArrayList<Scene>(1);
		this.loadedScene.add(null);
		this.setScene(this.constructDefaultScene());

		this.reloadProject();
	}

	/**
	 * Gets the currently loaded Scene in the Editor. There is always a scene
	 * loaded, so null will never be returned.
	 * 
	 * @return the loaded scene.
	 */
	public Scene getScene() {
		return this.loadedScene.get(0);
	}

	/**
	 * Loads a new scene and does the necessary cleanup on the previously loaded
	 * scene. Passing the scene that is currently loaded will perform a "reload"
	 * (scene is unloaded, then loaded again).
	 * 
	 * @param scene The scene to load. May not be null.
	 */
	public void setScene(Scene scene) {
		if (scene == null) {
			Debug.logError("Can not set Scene to null");
			return;
		}
		Scene oldScene = scene;

		this.loadedScene.set(0, scene);

		// Fire SceneChangeEvent.
		for (SceneChangeListener listener : this.listenerList.getListeners(SceneChangeListener.class)) {
			listener.onSceneChange(oldScene, scene);
		}
	}

	@Override
	public Iterable<Scene> getScenes() {
		return this.loadedScene;
	}

	public void saveScene() {
		Scene scene = this.getScene();
		this.assetDatabase.saveAsset(scene);

		// Raise event.
		for (ProjectSaveListener listener : this.listenerList.getListeners(ProjectSaveListener.class)) {
			listener.onSave();
		}
		
		this.properties.save();
	}

	public void preformUndo() {
		Debug.logWarning("Undo is not yet implemented."); // TODO implement undo.
	}

	public void preformRedo() {
		Debug.logWarning("Redo is not yet implemented."); // TODO implement redo.
	}

	public void enableEditorContext() {
		SceneAWTGLCanvas canvas = this.window.sceneView.canvas;
		canvas.makeContextCurrent(canvas.getCanvasContext());
	}

	public void disableEditorContext() {
		SceneAWTGLCanvas canvas = this.window.sceneView.canvas;
		canvas.makeContextCurrent(0);
	}

	public void reloadProject() {
		Debug.log("[Editor]: Reloading project.");

		for (Phase p : Phase.values()) {
			for (ProjectReloadListener listener : this.listenerList.getListeners(ProjectReloadListener.class)) {
				listener.onProjectReload(p);
			}
		}
	}

	/**
	 * Starts play mode.
	 * 
	 * @return {@link true} if play mode started, {@link false} if it could not be
	 *         for any reason.
	 */
	public boolean startPlaymode() {
		if (this.isInPlaying()) {
			return false;
		}

		this.application = new Application();
		this.application.onClose = () -> {
			this.application = null;
			this.firePlayModeEvent(State.STOPPED);
		};
		this.application.start(); // TODO start in the correct scene.

		this.firePlayModeEvent(State.STARTED);

		return true;
	}

	/**
	 * Stops the Application. The Application does not stop immediately, it will
	 * terminate on it's next frame a perform any necessary cleanup.
	 */
	public void stopPlaymode() {
		if (!this.isInPlaying()) {
			return;
		}

		this.application.stop();
		this.application = null;

		this.firePlayModeEvent(State.STOPPED);
	}

	/**
	 * Checks if the Editor is in play mode.
	 * 
	 * @return {@link true} if the Editor is in play mode, {@link false} if it is
	 *         not.
	 */
	public boolean isInPlaying() {
		return this.application != null;
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

	private Scene constructDefaultScene() {
		Scene scene = new Scene(this.assetsFolder.resolve("scene.jelobj"));

		GameObject cameraObj = scene.instantiateGameObject("Main Camera");
		cameraObj.addComponent(Camera.class);
		cameraObj.setPosition(0, 5, -10);
		cameraObj.setEulerAngles(20, 0, 0);

		GameObject meshObj = scene.instantiateGameObject("Cube");
		meshObj.addComponent(MeshRenderer.class);

		GameObject lightObj = scene.instantiateGameObject("Light");
		// meshObj.addComponent(Light.class);
		lightObj.setPosition(0, 0, -2);
		lightObj.setEulerAngles(20, 45, 0);

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

	private void firePlayModeEvent(State state) {
		for (PlayModeListener listener : this.listenerList.getListeners(PlayModeListener.class)) {
			listener.onPlaymodeChange(state);
		}
	}
}
