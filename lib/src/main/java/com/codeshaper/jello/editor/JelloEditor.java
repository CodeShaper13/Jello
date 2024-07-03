package com.codeshaper.jello.editor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWErrorCallback;

import com.codeshaper.jello.editor.property.drawer.FieldDrawerRegistry;
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
	public static final String REPORT_ISSUE_URL = "http://www.google.com";
	public static final String DOCUMENTAION_URL = "http://www.google.com";

	public static JelloEditor instance;

	public static void main(String[] args) {
		if (args.length > 0) {
			// A path was supplied.
			String path = args[0];
			SwingUtilities.invokeLater(() -> {
				new JelloEditor(new File(path));
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

	public final File rootProjectFolder;
	public final FieldDrawerRegistry filedDrawers;
	public final EditorMainFrame window;
	public final ILogHandler logHandler;
	public final Renderer renderer;

	/**
	 * This list always has a length of 1, the current scene. A list so it can be
	 * used by {@link ISceneProvider}.
	 */
	private final List<Scene> loadedScene;

	private JelloEditor(File projectFolder) {
		instance = this;

		// Create the /assets folder if it doesn't exist.
		this.rootProjectFolder = projectFolder;
		File file = new File(this.rootProjectFolder, "assets");
		if (!file.exists()) {
			file.mkdirs();
		}
		this.writeEditorVersionFile();

		this.filedDrawers = new FieldDrawerRegistry();
		this.filedDrawers.registerBuiltinDrawers();

		this.window = new EditorMainFrame(this);

		this.logHandler = this.window.console;

		this.renderer = this.window.sceneView.createContext();

		GLFWErrorCallback.createPrint().set();

		this.window.sceneView.makeContextCurrent();

		this.loadedScene = new ArrayList<Scene>(1);
		this.loadedScene.add(null);
		this.setScene(this.constructDefaultScene());
	}

	@SuppressWarnings("unchecked")
	public Class<JelloComponent>[] getComponents() {
		return new Class[] { AudioListener.class, AudioSource.class, Camera.class, FontRenderer.class, Light.class,
				LineRenderer.class, MeshRenderer.class, SpriteRenderer.class, TilemapRenderer.class, };
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

		this.loadedScene.set(0, scene);
		this.window.hierarchy.rebuildHierarchy();
	}

	@Override
	public Iterable<Scene> getScenes() {
		return this.loadedScene;
	}

	public void save() {
		Debug.logWarning("Saving is not yet implemented."); // TODO implement saving.
	}

	public void preformUndo() {
		Debug.logWarning("Undo is not yet implemented."); // TODO implement undo.
	}

	public void preformRedo() {
		Debug.logWarning("Redo is not yet implemented."); // TODO implement redo.
	}

	private Scene constructDefaultScene() {
		Scene scene = new Scene();

		GameObject cameraObj = scene.instantiateGameObject("Main Camera");
		cameraObj.addComponent(Camera.class);

		GameObject meshObj = scene.instantiateGameObject("Mesh");
		meshObj.addComponent(MeshRenderer.class);
		meshObj.setPosition(0, 0, -2);
		meshObj.setEulerAngles(20, 45, 0);
		
		//GameObject testObj = scene.instantiateGameObject("Test");
		//testObj.addComponent(Test.class);

		return scene;
	}

	/**
	 * Creates a version file that contains the version of the Editor.
	 */
	private void writeEditorVersionFile() {
		File file = new File(this.rootProjectFolder, "version.txt");
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			writer.write(EDITOR_VERSION);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
