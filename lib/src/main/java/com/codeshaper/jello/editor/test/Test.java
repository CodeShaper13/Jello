package com.codeshaper.jello.editor.test;

import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import org.lwjgl.opengl.awt.AWTGLCanvas;

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

public class Test extends JFrame {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			Test example = new Test();
			example.setVisible(true);
		});
	}
	
	public Mesh mesh;
	
	public Test() {		
		setTitle("Basic Modern Docking basic.Example");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
		setPreferredSize(new Dimension(1000, 800));
        
        Docking.initialize(this);
        
        AppState.setPersistFile(new File("testLayout.xml"));
     
        // restore the layout from the auto persist file after the UI has loaded
 		SwingUtilities.invokeLater(() -> {
 			try {
 				AppState.restore();
 			}
 			catch (DockingLayoutException e) {
 				e.printStackTrace();
 			}
 			// now that we've restored the layout we can turn on auto persist
 			AppState.setAutoPersist(true);
 		});
 		
        Panel panel1 = new Panel("one");
        Panel panel2 = new Panel("two");
        Panel panel3 = new Panel("three");
		//LwjglPanel panel4 = new LwjglPanel("four", Color.green);
    
        LwjglPanel lwjglPanel = new LwjglPanel("one", Color.red);
        
        
		RootDockingPanel rootPanel = new RootDockingPanel(this);

		this.add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, lwjglPanel, rootPanel), BorderLayout.CENTER);

		Docking.dock(panel1, this);
		Docking.dock(panel2, panel1, DockingRegion.CENTER);
		Docking.dock(panel3, panel2, DockingRegion.EAST);
		//Docking.dock(panel4, this, DockingRegion.SOUTH);		
		    
		this.setVisible(true);
		this.pack();
		this.transferFocus();
		
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("Window");    	
		LayoutsMenu layouts = new LayoutsMenu();
		menu.add(layouts);		
		for(Dockable dockable : Docking.getDockables()) {
			menu.add(new DockableMenuItem(
					dockable.getPersistentID(),
					"Open " + dockable.getTabText()));
		}
		menuBar.add(menu);
		this.setJMenuBar(menuBar);
		
		lwjglPanel.canvas.runInContext(() -> { lwjglPanel.canvas.initGL(); } );
				
		System.out.println("instantiating mesh...");
		/*
		float[] positions = new float[]{
                0.0f, 0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f
        };
        this.mesh = new Mesh(positions, 3);
        */
	}
	
	public class LwjglCanvas extends AWTGLCanvas {

		private final Color clearColor;
		private final Runnable loop;
		
		public LwjglCanvas(String name, Color clearColor) {	
			this.clearColor = clearColor;
			
			this.loop = new Runnable() {
				@Override
	            public void run() {					
					if(isValid()) {
						render();
					}					
					SwingUtilities.invokeLater(this);
				}
			};
			
			SwingUtilities.invokeLater(this.loop);
		}				
		
		@Override
        public void initGL() {
            System.out.println("initGL()");
            createCapabilities();
            glClearColor(this.clearColor.r, this.clearColor.g, this.clearColor.b, 1);
        
            this.initCalled = true;
		}
		
        @Override
        public void paintGL() {
        	if(this.getParent() instanceof LwjglPanel panel) {
        		panel.label.setText("Context: " + this.context);
        	}
        	
            int w = this.getWidth();
            int h = this.getHeight();
            float aspect = (float) w / h;
            double now = System.currentTimeMillis() * 0.001;
            float width = (float) Math.abs(Math.sin(now * 0.3));
            glClear(GL_COLOR_BUFFER_BIT);
            glViewport(0, 0, w, h);
            glBegin(GL_QUADS);
            glColor3f(1f, 0f, 0.8f);
            glVertex2f(-0.75f * width / aspect, 0.0f);
            glVertex2f(0, -0.75f);
            glVertex2f(+0.75f * width/ aspect, 0);
            glVertex2f(0, +0.75f);
            glEnd();
            swapBuffers();
        }
	}
	
	public class Panel extends JPanel implements Dockable {
		private final String name;

		public Panel(String name) {
			this.name = name;
			
			Docking.registerDockable(this);
		}

		@Override
		public boolean isWrappableInScrollpane() {
			return false;
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
	
	public class LwjglPanel extends JPanel {		
	        
        public LwjglCanvas canvas;
        public JLabel label;
        
        public LwjglPanel(String name, Color color) {        	
        	//super(name);
        	
        	this.setLayout(new BorderLayout());
        	
        	this.canvas = new LwjglCanvas(name, color);
        	this.add(canvas, BorderLayout.CENTER);
        	        	
        	System.out.println("LwjglPanel#ctor()");

        	this.label = new JLabel("Context: ?");
        	this.add(label, BorderLayout.NORTH);
        }
	}
}