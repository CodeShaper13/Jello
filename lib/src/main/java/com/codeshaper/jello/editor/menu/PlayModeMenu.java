package com.codeshaper.jello.editor.menu;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.event.PlayModeListener.State;

public class PlayModeMenu extends JMenu {

	public PlayModeMenu() {
		super("Play Mode");

		JMenuItem run = new JMenuItem("Start");
		run.setToolTipText("Starts Play Mode with the Scene set to the \"Main Scene\"");
		run.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		run.addActionListener((e) -> {
			JelloEditor.instance.startPlaymode();
		});
		this.add(run);

		JMenuItem runCurrent = new JMenuItem("Start Current");
		runCurrent.setToolTipText("Starts Play Mode with all of the currently open Scenes");
		runCurrent.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		runCurrent.addActionListener((e) -> {
			JelloEditor.instance.startPlaymode();
		});
		this.add(runCurrent);
		
		this.addSeparator();
		
		JMenuItem stop = new JMenuItem("Stop");
		stop.setToolTipText("Stops Play Mode");
		stop.addActionListener((e) -> {
			JelloEditor.instance.stopPlaymode();
		});
		stop.setEnabled(false);
		this.add(stop);
		
		JelloEditor.instance.addPlayModeListener((state) -> {
			if(state == State.STARTED) {
				run.setEnabled(false);
				runCurrent.setEnabled(false);
				stop.setEnabled(true);
			} else if(state == State.STOPPED) {
				run.setEnabled(true);
				runCurrent.setEnabled(true);
				stop.setEnabled(false);
			}
		});
	}
}
