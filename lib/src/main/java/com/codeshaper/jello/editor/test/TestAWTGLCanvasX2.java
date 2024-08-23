package com.codeshaper.jello.editor.test;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.awt.AWTGLCanvas;

import static org.lwjgl.opengl.GL.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * x2 AWTGLCanvas
 */
public class TestAWTGLCanvasX2 {

	public static void main(String[] args) {
		new TestAWTGLCanvasX2();
	}

	private AWTGLCanvas canvas1;
	private AWTGLCanvas canvas2;
	private GLXGears gears;
	private long c1;
	private long c2;

	public TestAWTGLCanvasX2() {
		JFrame frame = new JFrame("AWT test");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setLayout(new GridLayout(2, 1));
		frame.setPreferredSize(new Dimension(600, 600));

		frame.add(canvas1 = new AWTGLCanvas() {
			@Override
			public void initGL() {
				createCapabilities();
				gears = new GLXGears();
				c1 = this.context;
			}

			@Override
			public void paintGL() {
				gears.setSize(this.getWidth(), this.getHeight());
				gears.render();
				swapBuffers();
			}
		});

		frame.add(canvas2 = new AWTGLCanvas() {
			@Override
			public void initGL() {
				createCapabilities();
				c2 = this.context;				
				this.context = c1;
			}

			@Override
			public void paintGL() {
				gears.setSize(50, 50);
				gears.render();				
				swapBuffers();
			}
		});

		frame.pack();
		frame.setVisible(true);
		frame.transferFocus();
		
		SwingUtilities.invokeLater(() -> {
			glClearColor(1f, 0f, 0f, 1);

			Timer timer = new Timer();
			timer.scheduleAtFixedRate(new RunTask(), 0, (long) 1000 / 30);
		});
	}
	
	private class RunTask extends TimerTask {

		@Override
		public void run() {
			if (!canvas1.isValid()) {
				GL.setCapabilities(null);
				return;
			}
			
			if (gears != null) {
				gears.animate();
			}
			canvas1.render();
			canvas2.render();			
		}		
	}
}
