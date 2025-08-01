package com.codeshaper.jello.editor.window;

import java.awt.BorderLayout;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.event.ProjectReloadListener.Phase;
import com.codeshaper.jello.editor.inspector.Editor;
import com.codeshaper.jello.engine.AssetLocation;
import com.codeshaper.jello.engine.CustomEditor;
import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.JelloObject;
import com.codeshaper.jello.engine.database.AssetDatabase;

public class InspectorWindow extends EditorWindow {

	private static final String SELECTED_ASSET_KEY = "window.inspector.selectedAsset";
	
	private JelloObject target;
	private Editor<?> editor;
	private JPanel panel;
	private String persistencePath;

	public InspectorWindow() {
		super("Inspector", "inspector");

		this.panel = new JPanel();
		this.setLayout(new BorderLayout());
		
		//JelloObject obj = this.func(JelloEditor.instance.properties.getString(SELECTED_ASSET_KEY, ""));
		//System.out.println(JelloEditor.instance.properties.getString(SELECTED_ASSET_KEY, "") + " " + obj);
		//if(obj != null) {
		//	this.setTarget(obj);
		//}
		
		JelloEditor.instance.addProjectReloadListener((phase) -> {
			if(phase == Phase.PRE_REBUILD) {
				if(this.target != null) {
					this.persistencePath = this.target.getPersistencePath();
				} else {
					this.persistencePath = null;
				}
				this.setTarget(null);
			} else if(phase == Phase.POST_REBUILD) {
				if(this.persistencePath != null) {
					this.func(this.persistencePath);
				}
			}
		});
	}
	
	private JelloObject func(String s) {
		if(s == null) {
			return null;
		}
		
		if(s.startsWith("[Asset]")) {
			return AssetDatabase.getInstance().getAsset(new AssetLocation(s.substring(7)));
		} else if(s.startsWith("[GameObject]")) {
			GameObject obj = GameObject.fromPersistantPath(s);
			if(obj != null) {
				this.setTarget(obj);
			}
		}
		
		return null;
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
	public void setTarget(JelloObject object) {
		// Let the previous editor perform any cleanup that it needs to do.
		if (this.editor != null) {
			this.editor.cleanup();
			this.editor = null;
		}

		this.remove(this.panel);
		this.panel = new JPanel();
		this.add(this.panel, BorderLayout.CENTER);

		this.target = object;
		if (this.target != null) {
			// Create a new editor.
			CustomEditor annotation = this.target.getClass().getAnnotation(CustomEditor.class);
			if(annotation != null) {
				Class<? extends Editor> cls = annotation.value();
				try {
					// TODO bulletproof this in the event of the passed class being inner/private/whatever
					this.editor = cls.getDeclaredConstructor(this.target.getClass(), JPanel.class).newInstance(this.target, this.panel);
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
				}
			} else {
				this.editor = this.target.getEditor(this.panel);
			}
			
			//this.editor = this.target.getInspectorDrawer(this.panel);
			this.editor.create();
			JelloEditor.instance.properties.setString(SELECTED_ASSET_KEY, this.target.getPersistencePath());
		}

		this.validate();
	}

	/**
	 * Gets the object that the Inspector is looking at. May be null.
	 * 
	 * @return the object the Inspector is looking at.
	 */
	public JelloObject getTarget() {
		return this.target;
	}

	/**
	 * Refreshes the Editor of the Inspector's Target. If the Inspector has no
	 * target, or the target did not provide an Editor, nothing happens.
	 */
	public void refresh() {
		if(this.target != null) {
			this.setTarget(this.target);
		}
	}
}
