package com.codeshaper.jello.editor;

import static org.lwjgl.opengl.GL11.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Callable;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

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
	
	/**
	 * The Camera that draws the scene.
	 */
	public final Camera sceneCamera;
	public final SceneAWTGLCanvas canvas;

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
			if(toggleWireframe.isSelected()) {
				glPolygonMode(GL11.GL_FRONT_AND_BACK, GL_LINE);
			} else {
				glPolygonMode(GL11.GL_FRONT_AND_BACK, GL_FILL);
			}
			
			JelloEditor.instance.renderer.render(JelloEditor.instance, sceneCamera, this.getWidth(), this.getHeight());
			
			// this.func034();

			this.swapBuffers();
		}
		
		public void makeContextCurrent() {
			this.platformCanvas.makeCurrent(this.context);
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
