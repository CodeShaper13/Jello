package com.codeshaper.jello.editor.window;

import java.awt.BorderLayout;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import com.codeshaper.jello.editor.inspector.Editor;
import com.codeshaper.jello.editor.inspector.IInspectable;

public class InspectorWindow extends EditorWindow {

	private IInspectable target;
	private Editor<?> editor;
	private JPanel panel;

	public InspectorWindow() {
		super("Inspector", "inspector");

		this.panel = new JPanel();
		this.setLayout(new BorderLayout());
	}

	@Override
	public boolean isWrappableInScrollpane() {
		return false;
	}

	@Override
	public boolean getHasMoreOptions() {
		return true;
	}

	@Override
	public void addMoreOptions(JPopupMenu menu) {
		JMenuItem refresh = new JMenuItem("Refresh");
		refresh.addActionListener((e) -> this.refresh());
		menu.add(refresh);
	}

	/**
	 * Sets the object that the Inspector is looking at.
	 * 
	 * @param object the object to look at, or null.
	 */
	public void setTarget(IInspectable object) {
		// Let the previous editor perform any cleanup that it needs to do.
		if (this.editor != null) {
			this.editor.onCleanup();
			this.editor = null;
		}

		this.remove(this.panel);
		this.panel = new JPanel();
		this.add(this.panel, BorderLayout.CENTER);

		this.target = object;
		if (this.target != null) {
			// Create a new editor.
			this.editor = this.target.getInspectorDrawer(this.panel);
		}

		this.validate();
	}

	/**
	 * Gets the object that the Inspector is looking at. May be null.
	 * 
	 * @return the object the Inspector is looking at.
	 */
	public IInspectable getTarget() {
		return this.target;
	}

	/**
	 * Refreshes the Editor of the Inspector's Target. If the Inspector has no
	 * target, or the target did not provide an Editor, nothing happens.
	 */
	public void refresh() {
		if (this.editor != null) {
			this.editor.onRefresh();
		}
	}
}
