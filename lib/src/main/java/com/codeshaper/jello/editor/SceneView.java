package com.codeshaper.jello.editor;

import static org.lwjgl.opengl.GL11.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.concurrent.Callable;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;

import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.Perspective;
import com.codeshaper.jello.engine.asset.Scene;
import com.codeshaper.jello.engine.component.Camera;
import com.codeshaper.jello.engine.component.JelloComponent;
import com.codeshaper.jello.engine.render.Renderer;

public class SceneView extends JPanel {

	private JToggleButton toggleLighting;
	private JToggleButton toggleWireframe;
	private JToggleButton toggleGrid;
	private JToggleButton toggleGizmos;
	private final CameraController cc;
	private final SceneAWTGLCanvas canvas;

	/**
	 * The Camera that draws the scene.
	 */
	public final Camera sceneCamera;

	public SceneView() {
		this.setLayout(new BorderLayout());

		this.sceneCamera = new Camera(null);

		this.add(this.canvas = new SceneAWTGLCanvas(new GLData()), BorderLayout.CENTER);
		this.add(this.createToolbar(), BorderLayout.NORTH);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (!canvas.isValid()) {
					System.out.println("Clearing Capabilities");
					GL.setCapabilities(null);
					return;
				}

				if (isValid()) {
					canvas.render();
				}

				SwingUtilities.invokeLater(this);
			}
		});

		this.cc = new CameraController();
		this.canvas.addMouseMotionListener(this.cc);
		this.canvas.addMouseListener(this.cc);
		this.canvas.addMouseWheelListener(this.cc);
	}

	@Override
	public Dimension getMinimumSize() {
		return new Dimension(100, 200);
	}

	public Renderer createContext() {
		// Hacky way of making LWJGL create a context immediately.
		try {
			return this.canvas.executeInContext(new Callable<Renderer>() {
				@Override
				public Renderer call() throws Exception {
					canvas.initGL();
					return new Renderer();
				}

			});
		} catch (Exception e) {
		} // Never happens.
		return null;
	}

	public void makeContextCurrent() {
		this.canvas.makeContextCurrent();
	}

	private JToolBar createToolbar() {
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);

		this.toggleLighting = new JToggleButton("Lighting");
		toolbar.add(this.toggleLighting);

		this.toggleWireframe = new JToggleButton("Wireframe");
		toolbar.add(this.toggleWireframe);

		this.toggleGrid = new JToggleButton("Grid");
		toolbar.add(this.toggleGrid);

		this.toggleGizmos = new JToggleButton("Gizmos");
		toolbar.add(this.toggleGizmos);

		JComboBox<Perspective> comboBoxPerspective = new JComboBox<Perspective>(Perspective.values());
		comboBoxPerspective.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (comboBoxPerspective.getSelectedIndex() == 0) {
					sceneCamera.perspective = Perspective.PERSPECTVE;
				} else {
					sceneCamera.perspective = Perspective.ORTHOGRAPHIC;
				}
			}
		});
		toolbar.add(comboBoxPerspective);

		return toolbar;
	}

	public class SceneAWTGLCanvas extends AWTGLCanvas {

		public SceneAWTGLCanvas(GLData glData) {
			super(glData);
		}

		@Override
		public void initGL() {
			this.initCalled = true;
		}

		@Override
		public void paintGL() {
			if (toggleWireframe.isSelected()) {
				glPolygonMode(GL11.GL_FRONT_AND_BACK, GL_LINE);
			} else {
				glPolygonMode(GL11.GL_FRONT_AND_BACK, GL_FILL);
			}

			JelloEditor.instance.renderer.render(JelloEditor.instance, sceneCamera, cc.getViewMatrix(), this.getWidth(), this.getHeight());

			this.swapBuffers();
		}

		public void makeContextCurrent() {
			this.platformCanvas.makeCurrent(this.context);
		}
	}

	/**
	 * Provides controls for moving 
	 * 
	 * Controls:
	 * Scroll Wheel: Zooms in and out.
	 * MMB + Move Cursor: Pan camera.
	 * RMB + Move Cursor: Rotate camera.
	 */
	private class CameraController implements MouseListener, MouseMotionListener, MouseWheelListener {

		private static final float ZOOM_SPEED = 1f;
		private static final float ROTATE_SPEED = 0.01f;
		private static final float PAN_SPEED = 0.02f;
		
		private Vector3f position;
		private float xRot = 0f;
		private float yRot = 0f;
		private Matrix4f viewMatrix;
		
		/**
		 * Is the middle mouse button pressed?
		 */
		private boolean isMMBDown = false;
		/**
		 * Is the right mouse button pressed?
		 */
		private boolean isRMBDown = false;
		private Point pointLastPos;

		public CameraController() {
			this.position = new Vector3f();
			this.viewMatrix = new Matrix4f();
		}

		public Matrix4f getViewMatrix() {			
			return this.viewMatrix;
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {			
			float scroll = e.getWheelRotation();
			Vector3f direction = new Vector3f();
			this.viewMatrix.positiveZ(direction).mul(scroll * ZOOM_SPEED);
			this.position.add(direction);
			this.recalculate();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			Point current = e.getPoint();

			Point motion = new Point(current.x - this.pointLastPos.x,  current.y - this.pointLastPos.y);			
			
			Vector3f vec = new Vector3f();

			if(this.isMMBDown) {
				// Pan.
				
				// Up/down.
				viewMatrix.positiveY(vec).mul(motion.y * PAN_SPEED);
		        position.add(vec);
		        
		        // Left/right.
		        viewMatrix.positiveX(vec).mul((motion.x * -1) * PAN_SPEED);
		        position.add(vec);
			}
			
			if(this.isRMBDown) {
				// Rotate.
				this.xRot += motion.y * -1f * ROTATE_SPEED;
				this.yRot += motion.x * -1f * ROTATE_SPEED;
			}
	        
	        this.recalculate();
			
			this.pointLastPos = current;
		}

		@Override
		public void mouseMoved(MouseEvent e) { }

		@Override
		public void mouseClicked(MouseEvent e) { }

		@Override
		public void mousePressed(MouseEvent e) {
			this.pointLastPos = e.getPoint();
			
			if(e.getButton() == MouseEvent.BUTTON3) {
				this.isRMBDown = true;
			}
			
			if(e.getButton() == MouseEvent.BUTTON2) {
				this.isMMBDown = true;
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {			
			if(e.getButton() == MouseEvent.BUTTON3) {
				this.isRMBDown = false;
			}
			
			if(e.getButton() == MouseEvent.BUTTON2) {
				this.isMMBDown = false;
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) { }

		@Override
		public void mouseExited(MouseEvent e) { }
		
		private void recalculate() {
			this.viewMatrix.identity().rotateX(this.xRot).rotateY(this.yRot);
			this.viewMatrix.translate(-position.x, -position.y, -position.z);
		}
	}

	// Unused.
	private void func() {
		Scene scene = JelloEditor.instance.getScene();
		for (GameObject object : scene.getRootGameObjects()) {
			for (JelloComponent component : object.getAllComponents()) {
				GL11.glPushMatrix();
				GL11.glEnable(GL_CULL_FACE);

				Vector3f scale = object.getScale();
				GL11.glScaled(scale.x, scale.y, scale.z);

				// TODO rotation
				GL11.glRotatef(30.0F, 1.0F, 0.0F, 0.0F);
				double now = System.nanoTime() / 1_000_000_000f;
				GL11.glRotatef(45f, 0f, 1f, 0f);
				GL11.glRotatef((float) now * 30f, 0f, 1f, 0f);

				Vector3f pos = object.getPosition();
				GL11.glTranslated(pos.x, pos.y, pos.z);

				component.onRender();

				GL11.glPopMatrix();
			}
		}
	}
}
