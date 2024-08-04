package com.codeshaper.jello.editor;

import static org.lwjgl.opengl.GL30.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
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
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;

import com.codeshaper.jello.engine.Perspective;
import com.codeshaper.jello.engine.component.Camera;
import com.codeshaper.jello.engine.rendering.GameRenderer;
import com.codeshaper.jello.engine.rendering.ShaderProgram;
import com.codeshaper.jello.engine.rendering.UniformsMap;

public class SceneView extends JPanel {

	private final SceneViewToolbar toolbar;
	private final CameraController cc;
	
	public final SceneAWTGLCanvas canvas;
	/**
	 * The Camera that draws the scene.
	 */
	public final Camera sceneCamera;

	public SceneView() {
		this.setLayout(new BorderLayout());

		this.sceneCamera = new Camera(null);
		this.canvas = new SceneAWTGLCanvas(new GLData());
		this.toolbar = new SceneViewToolbar();

		this.add(this.toolbar, BorderLayout.NORTH);
		this.add(this.canvas, BorderLayout.CENTER);

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

	public GameRenderer createContext() {
		// Hacky way of making LWJGL create a context immediately.
		try {
			return this.canvas.executeInContext(new Callable<GameRenderer>() {
				@Override
				public GameRenderer call() throws Exception {
					GameRenderer renderer = new GameRenderer();
					canvas.initGL();
					return renderer;
				}

			});
		} catch (Exception e) {
		} // Never happens.
		return null;
	}
	
	private class SceneViewToolbar extends JToolBar {

		private final JToggleButton toggleLighting;
		private final JToggleButton toggleWireframe;
		private final JToggleButton toggleGrid;
		private final JToggleButton toggleGizmos;
		private final JComboBox<Perspective> dropdownPerspective;
		
		public SceneViewToolbar() {
			this.setFloatable(false);
			
			this.toggleLighting = new JToggleButton("Lighting");
			this.toggleLighting.setSelected(true);
			this.add(this.toggleLighting);

			this.toggleWireframe = new JToggleButton("Wireframe");
			this.add(this.toggleWireframe);

			this.toggleGrid = new JToggleButton("Grid");
			this.toggleGrid.setSelected(true);
			this.add(this.toggleGrid);

			this.toggleGizmos = new JToggleButton("Gizmos");
			this.toggleGizmos.setSelected(true);
			this.add(this.toggleGizmos);

			this.dropdownPerspective = new JComboBox<Perspective>(Perspective.values());
			this.add(this.dropdownPerspective);
		}
		
		public boolean isLightingEnabled() {
			return this.toggleLighting.isEnabled();
		}
		
		public boolean isWireframeEnabled() {
			return this.toggleWireframe.isSelected();
		}
		
		public boolean isGridEnabled() {
			return this.toggleGrid.isSelected();
		}
		
		public boolean isGizmosEnabled() {
			return this.toggleGizmos.isSelected();
		}

		public Perspective getPerspective() {
			return this.dropdownPerspective.getSelectedIndex() == 0 ? Perspective.PERSPECTVE : Perspective.ORTHOGRAPHIC;
		}
	}

	/**
	 * Provides controls for moving the scene view camera.
	 * 
	 * Controls: Scroll Wheel: Zooms in and out. MMB + Move Cursor: Pan camera. RMB
	 * + Move Cursor: Rotate camera.
	 */
	public class CameraController implements MouseListener, MouseMotionListener, MouseWheelListener {

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
			EditorProperties props = JelloEditor.instance.properties;

			this.position = new Vector3f(
					props.getFloat("camera.position.x", 0),
					props.getFloat("camera.position.y", 0),
					props.getFloat("camera.position.z", 0));
			this.xRot = props.getFloat("camera.rotation.x", 0);
			this.yRot = props.getFloat("camera.rotation.y", 0);
			
			this.viewMatrix = new Matrix4f();
			
			this.recalculate();
			
			JelloEditor.instance.addProjectSaveListener(() -> this.saveToProperties());
		}

		/**
		 * Gets the Camera's view matrix.
		 * 
		 * @return the Camera's view matrix.
		 */
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
			Point motion = new Point(current.x - this.pointLastPos.x, current.y - this.pointLastPos.y);

			if (this.isMMBDown) {
				// Pan.
				Vector3f vec = new Vector3f();
				
				// Up/down.
				this.viewMatrix.positiveY(vec).mul(motion.y * PAN_SPEED);
				this.position.add(vec);

				// Left/right.
				this.viewMatrix.positiveX(vec).mul((motion.x * -1) * PAN_SPEED);
				this.position.add(vec);
			}

			if (this.isRMBDown) {
				// Rotate.
				this.xRot += motion.y * -1f * ROTATE_SPEED;
				this.yRot += motion.x * -1f * ROTATE_SPEED;
			}

			this.recalculate();

			this.pointLastPos = current;
		}

		@Override
		public void mouseMoved(MouseEvent e) {
		}

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			this.pointLastPos = e.getPoint();

			if (e.getButton() == MouseEvent.BUTTON3) {
				this.isRMBDown = true;
			}

			if (e.getButton() == MouseEvent.BUTTON2) {
				this.isMMBDown = true;
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON3) {
				this.isRMBDown = false;
			}

			if (e.getButton() == MouseEvent.BUTTON2) {
				this.isMMBDown = false;
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		/**
		 * Recalculates the controllers view matrix. This should be called whenever the
		 * {@link CameraController#position}, {@link CameraController#xRot}, or
		 * {@link CameraController#yRot} is changed.
		 */
		private void recalculate() {
			this.viewMatrix.identity().rotateX(this.xRot).rotateY(this.yRot);
			this.viewMatrix.translate(-this.position.x, -this.position.y, -this.position.z);
		}
		
		private void saveToProperties() {
			EditorProperties props = JelloEditor.instance.properties;
			props.setFloat("camera.position.x", this.position.x);
			props.setFloat("camera.position.y", this.position.y);
			props.setFloat("camera.position.z", this.position.z);
			props.setFloat("camera.rotation.x", this.xRot);
			props.setFloat("camera.rotation.y", this.yRot);
		}
	}

	public class SceneAWTGLCanvas extends AWTGLCanvas {
		
		private static final String NEAR = "near";
		private static final String FAR = "far";

	    private ShaderProgram gridShaderProgram;
	    private UniformsMap gridUniformsMap;
		
		public SceneAWTGLCanvas(GLData glData) {
			super(glData);
		}

		@Override
		public void initGL() {
	        this.gridShaderProgram = new ShaderProgram(
	        		ShaderProgram.ShaderModuleData.fromResources("/editorGridShaders/editorGrid.vert", GL_VERTEX_SHADER),
	        		ShaderProgram.ShaderModuleData.fromResources("/editorGridShaders/editorGrid.frag", GL_FRAGMENT_SHADER));
	        this.gridUniformsMap = new UniformsMap(gridShaderProgram.getProgramId());
	        this.gridUniformsMap.createUniform(GameRenderer.PROJECTION_MATRIX);
	        this.gridUniformsMap.createUniform(GameRenderer.VIEW_MATRIX);
	        this.gridUniformsMap.createUniform(NEAR);
	        this.gridUniformsMap.createUniform(FAR);
			
			this.initCalled = true;
		}

		@Override
		public void paintGL() {
			boolean isWireframeEnabled = toolbar.isWireframeEnabled();
			if (isWireframeEnabled) {
				glPolygonMode(GL30.GL_FRONT_AND_BACK, GL30.GL_LINE);
			} else {
				glPolygonMode(GL30.GL_FRONT_AND_BACK, GL30.GL_FILL);
			}
			
			sceneCamera.perspective = toolbar.getPerspective();

			Matrix4f viewMatrix = cc.getViewMatrix();
			JelloEditor.instance.renderer.render(JelloEditor.instance.sceneManager, sceneCamera, viewMatrix, this.getWidth(), this.getHeight());

			if(toolbar.isGridEnabled()) {
				if(isWireframeEnabled) {
					glPolygonMode(GL30.GL_FRONT_AND_BACK, GL30.GL_FILL);
				}
				this.drawGrid(sceneCamera, viewMatrix);			
			}
			
			this.swapBuffers();
		}
		
		@Override
		protected void afterRender() {
			// TODO Auto-generated method stub
			super.afterRender();
		
			this.platformCanvas.makeCurrent(this.context);
		}

		public long getCanvasContext() {
			return this.context;
		}

		public void makeContextCurrent(long context) {
			//this.platformCanvas.makeCurrent(this.context);
		}
		
		private void drawGrid(Camera camera, Matrix4f viewMatrix) {
			this.gridShaderProgram.bind();
	        this.gridUniformsMap.setUniform(GameRenderer.PROJECTION_MATRIX, camera.getProjectionMatrix());   
	        this.gridUniformsMap.setUniform(GameRenderer.VIEW_MATRIX, viewMatrix);
	        this.gridUniformsMap.setUniform(NEAR, camera.nearPlane);
	        this.gridUniformsMap.setUniform(FAR, camera.farPlane);  
	        
	        glBegin(GL_QUADS);
	        glVertex2f(-1f, -1f);
	        glVertex2f(-1f, 1f);
	        glVertex2f(1f, 1f);
	        glVertex2f(1f, -1f);
	        glEnd(); 
	        
	        this.gridShaderProgram.unbind();
		}
	}
}
