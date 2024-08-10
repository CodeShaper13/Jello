package com.codeshaper.jello.editor.render;

import javax.swing.JComboBox;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import com.codeshaper.jello.engine.rendering.Perspective;

public class SceneViewToolbar extends JToolBar {

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
