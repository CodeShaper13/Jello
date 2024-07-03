package com.codeshaper.jello.editor.test;

import org.lwjgl.glfw.*;
import com.codeshaper.jello.engine.Color;
import com.codeshaper.jello.engine.asset.Mesh;

import ModernDocking.Dockable;
import ModernDocking.DockingRegion;
import ModernDocking.app.AppState;
import ModernDocking.app.DockableMenuItem;
import ModernDocking.app.Docking;
import ModernDocking.app.LayoutsMenu;
import ModernDocking.app.RootDockingPanel;
import ModernDocking.exception.DockingLayoutException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public final class Test2 extends JFrame {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			Test2 example = new Test2();
			example.setVisible(true);
		});
	}
	
	public Test2() {
		setTitle("Basic Modern Docking basic.Example");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(1000, 800));

		Docking.initialize(this);
		AppState.setPersistFile(new File("testLayout2.xml"));

		GLFWErrorCallback.createPrint().set();
		if (!glfwInit()) {
			throw new IllegalStateException("Unable to initialize glfw");
		}

		Panel panel1 = new Panel("one");
		Panel panel2 = new Panel("two");
		//Panel panel3 = new Panel("three");
		//Panel panel4 = new Panel("four");
		//Panel panel5 = new Panel("five");

		RootDockingPanel rootPanel = new RootDockingPanel(this);

		LwjglPanel lwjglPanel = new LwjglPanel("", Color.red);
                
		//JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, lwjglPanel, rootPanel);
		//this.add(splitPane);
		// JSplitPane(JSplitPane.VERTICAL_SPLIT, lwjglPanel, otherPanel), rootPanel));
		this.add(rootPanel);

		Docking.dock(panel1, this);
		Docking.dock(panel2, panel1, DockingRegion.CENTER);
		//Docking.dock(panel3, panel2, DockingRegion.EAST);
		//Docking.dock(panel4, this, DockingRegion.SOUTH);

		Docking.dock(lwjglPanel, this);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				System.out.println("stop 1");
				lwjglPanel.canvas.destroy();
			}
		});

		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE && e.getID() == KeyEvent.KEY_PRESSED) {
				System.out.println("stop 2");
				this.dispose();

				glfwTerminate();
				Objects.requireNonNull(glfwSetErrorCallback(null)).free();

				return true;
			}

			return false;
		});

		this.pack();
		this.setVisible(true);

		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("Window");
		LayoutsMenu layouts = new LayoutsMenu();
		menu.add(layouts);
		for (Dockable dockable : Docking.getDockables()) {
			menu.add(new DockableMenuItem(dockable.getPersistentID(), "Open " + dockable.getTabText()));
		}
		menuBar.add(menu);
		this.setJMenuBar(menuBar);

		SwingUtilities.invokeLater(() -> {
			try {
				AppState.restore();
			} catch (DockingLayoutException e) {
				e.printStackTrace();
			}
			AppState.setAutoPersist(true);
		});
				
		SwingUtilities.invokeLater(() -> {
			System.out.println("maing mesh");
			glfwInit();
			
			/*
	        float[] positions = new float[]{
	                0.0f, 0.5f, 0.0f,
	                -0.5f, -0.5f, 0.0f,
	                0.5f, -0.5f, 0.0f
	        };
	        @SuppressWarnings("unused")
			Mesh mesh = new Mesh(positions, 3);
			*/
		});
	}

	public class Panel extends JPanel implements Dockable {
		private final String name;

		public Panel(String name) {
			this.name = name;

			Docking.registerDockable(this);
		}

		@Override
		public String getPersistentID() {
			return name;
		}

		@Override
		public String getTabText() {
			return name;
		}
	}

	public class LwjglPanel extends Panel {

		public LWJGLCanvas canvas;

		public LwjglPanel(String name, Color color) {
			super(name);

			this.setLayout(new BorderLayout());

			this.canvas = new LWJGLCanvas() {
				@Override
				public void render(int width, int height) {
					super.render(width, height);

					int w = width;
					int h = height;
					float aspect = (float) w / h;
					double now = System.currentTimeMillis() * 0.001;
					float width2 = (float) Math.abs(Math.sin(now * 0.3));
					glClear(GL_COLOR_BUFFER_BIT);
					glViewport(0, 0, w, h);
					glBegin(GL_QUADS);
					glColor3f(1f, 0f, 0.8f);
					glVertex2f(-0.75f * width2 / aspect, 0.0f);
					glVertex2f(0, -0.75f);
					glVertex2f(+0.75f * width2 / aspect, 0);
					glVertex2f(0, +0.75f);
					glEnd();
				}
			};
			this.add(this.canvas);
		}
		
		/*
		@Override
		public boolean isWrappableInScrollpane() {
			return false;
		}

		@Override
		public boolean isClosable() {
			return false;
		}

		@Override
		public boolean isFloatingAllowed() {
			return false;
		}

		@Override
		public boolean isLimitedToRoot() {
			return true;
		}
		*/

		//@Override
		//public int getTabPosition() {
		//	return SwingConstants.TOP;
		//}
	}
}