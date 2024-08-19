package com.codeshaper.jello.editor.render;

import javax.swing.JComboBox;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import com.codeshaper.jello.editor.EditorProperties;
import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.engine.rendering.Perspective;

public class SceneViewToolbar extends JToolBar {

	private final JToggleButton toggleLighting;
	private final JToggleButton toggleWireframe;
	private final JToggleButton toggleGrid;
	private final JToggleButton toggleGizmos;
	private final JComboBox<Perspective> dropdownPerspective;
	private final JToggleButton toggleRedrawInPlayMode;

	public SceneViewToolbar() {
		this.setFloatable(false);

		this.toggleLighting = this.makeToggle("Lighting", "window.sceneView.showLighting", true);
		this.toggleWireframe = this.makeToggle("Wireframe", "window.sceneView.wireframeMode", false);
		this.toggleGrid = this.makeToggle("Grid", "window.sceneView.showGrid", true);
		this.toggleGizmos = this.makeToggle("Gizmos", "window.sceneView.showGizmos", true);

		this.dropdownPerspective = new JComboBox<Perspective>(Perspective.values());
		this.add(this.dropdownPerspective);

		this.toggleRedrawInPlayMode = this.makeToggle("Redraw In Play Mode", "window.sceneView.redrawInPlayMode", true);
	}

	private JToggleButton makeToggle(String label, String propsKey, boolean defaultValue) {
		EditorProperties props = JelloEditor.instance.properties;

		JToggleButton toggle = new JToggleButton(label);
		toggle.setSelected(props.getBoolean(propsKey, defaultValue));
		toggle.addActionListener(e -> {
			props.setBoolean(propsKey, toggle.isSelected());
		});
		this.add(toggle);

		return toggle;
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
	
	public boolean redrawInPlayMode() {
		return this.toggleRedrawInPlayMode.isSelected();
	}
}
