package com.codeshaper.jello.editor.render;

import static org.lwjgl.opengl.GL30.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL;

import com.codeshaper.jello.editor.GizmoDrawer;
import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.swing.AWTGLCanvasContextControl;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.JelloComponent;
import com.codeshaper.jello.engine.Scene;
import com.codeshaper.jello.engine.rendering.Camera;

public class SceneViewPanel extends JPanel {

	private final int FPS = 20;

	private final EditorCameraController cameraController;
	private final SceneViewToolbar toolbar;
	private final SceneAWTGLCanvas canvas;

	/**
	 * The Camera that draws the scene.
	 */
	public final Camera sceneCamera;

	public SceneViewPanel() {
		this.setLayout(new BorderLayout());

		this.sceneCamera = new Camera(null);
		this.sceneCamera.setFarPlane(10_000f);

		this.canvas = new SceneAWTGLCanvas();
		this.canvas.keepContextCurrent = true;

		this.add(this.toolbar = new SceneViewToolbar(), BorderLayout.NORTH);
		this.add(this.canvas, BorderLayout.CENTER);

		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				SwingUtilities.invokeLater(() -> {
					if (!canvas.isValid()) {
						GL.setCapabilities(null);
						timer.cancel();
						return;
					}

					canvas.render();
				});
			}
		}, 0, 1000L / FPS);

		this.cameraController = new EditorCameraController();
		this.canvas.addMouseMotionListener(this.cameraController);
		this.canvas.addMouseListener(this.cameraController);
		this.canvas.addMouseWheelListener(this.cameraController);
	}

	@Override
	public Dimension getMinimumSize() {
		return new Dimension(100, 50);
	}

	public long getContext() {
		return this.canvas.getContext();
	}

	public void createContext() {
		this.canvas.createContextImmediately();
	}

	public class SceneAWTGLCanvas extends AWTGLCanvasContextControl {

		private GizmoDrawer gizmoDrawer;
		private InfiniteGrid infiniteGrid;

		public SceneAWTGLCanvas() {
			super();

			this.gizmoDrawer = new GizmoDrawer();
		}

		@Override
		public void initGL() {
			this.infiniteGrid = new InfiniteGrid();
		}

		@Override
		public void paintGL() {
			boolean isWireframeEnabled = toolbar.isWireframeEnabled();
			if (isWireframeEnabled) {
				glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
			} else {
				glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
			}

			sceneCamera.perspective = toolbar.getPerspective();

			Matrix4f viewMatrix = cameraController.getViewMatrix();
			JelloEditor.instance.renderer.render(JelloEditor.instance.sceneManager, sceneCamera, viewMatrix,
					this.getWidth(), this.getHeight());

			if (isWireframeEnabled) {
				glPolygonMode(GL_FRONT_AND_BACK, GL_FILL); // Turn wire frame off for gizmos.
			}

			if (toolbar.isGizmosEnabled()) {
				glDisable(GL_DEPTH_TEST);
				this.drawGizmos(sceneCamera, viewMatrix);
				glEnable(GL_DEPTH_TEST);
			}

			if (toolbar.isGridEnabled()) {
				if (isWireframeEnabled) {
					glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
				}

				this.infiniteGrid.drawGrid(sceneCamera, viewMatrix);
			}

			this.swapBuffers();
		}

		private void drawGizmos(Camera sceneCamera, Matrix4f viewMatrix) {
			// TODO optimize to avoid memory allocation.
			Matrix4f projection = sceneCamera.getProjectionMatrix();
			Matrix4f m = new Matrix4f(projection);
			m.mul(viewMatrix);
			glLoadMatrixf(m.get(new float[16]));

			GameObject selectedGameObject = JelloEditor.instance.window.hierarchy.getSelected();

			for (Scene scene : JelloEditor.instance.sceneManager.getScenes()) {
				for (GameObject obj : scene.getRootGameObjects()) {
					this.callOnDrawGizmosRecursivly(obj, selectedGameObject);
				}
			}
		}

		private void callOnDrawGizmosRecursivly(GameObject gameObject, GameObject selectedGameObject) {
			if (!gameObject.isActive()) {
				return;
			}

			for (JelloComponent component : gameObject.getAllComponents()) {
				if (component.isEnabled()) {
					this.gizmoDrawer.reset();
					try {
						component.onDrawGizmos(this.gizmoDrawer, gameObject == selectedGameObject);
					} catch(Exception exception) {
						Debug.log(exception);
					}
				}
			}

			for (GameObject child : gameObject.getChildren()) {
				this.callOnDrawGizmosRecursivly(child, selectedGameObject);
			}
		}
	}
}
