package com.codeshaper.jello.engine.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.codeshaper.jello.engine.ComponentIcon;
import com.codeshaper.jello.engine.ComponentName;
import com.codeshaper.jello.engine.JelloComponent;

@ComponentName("Ui/Toggle Group")
@ComponentIcon("/editor/componentIcons/toggleGroup.png")
public class ToggleGroup extends JelloComponent {

	public boolean allowAllOff;

	List<Toggle> toggles = new ArrayList<Toggle>();

	/**
	 * Gets all {@link Toggle}s that are part of this Toggle Group.
	 * 
	 * @return All {@link Toggle}s that are part of this Toggle Group.
	 */
	public List<Toggle> getToggles() {
		return Collections.unmodifiableList(this.toggles);
	}
}
