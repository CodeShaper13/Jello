package com.codeshaper.jello.engine.gui;

import com.codeshaper.jello.editor.property.modifier.ExposeField;
import com.codeshaper.jello.engine.ComponentIcon;
import com.codeshaper.jello.engine.ComponentName;
import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.GameObjectReference;
import com.codeshaper.jello.engine.Input;
import com.codeshaper.jello.engine.event.JelloEvent;

@ComponentName("Ui/Element/Toggle")
@ComponentIcon("/editor/componentIcons/toggle.png")
public class Toggle extends UiBase {

	@ExposeField
	private boolean isOn;
	@ExposeField
	private ToggleGroup toggleGroup;
	public JelloEvent onToggle = new JelloEvent();
	
	@Override
	protected void onStart() {
		if(this.toggleGroup != null) {
			this.toggleGroup.toggles.add(this);
		}
	}
	
	@Override
	protected void onDestroy() {
		if(this.toggleGroup != null) {
			this.toggleGroup.toggles.remove(this);
		}
	}
	
	@Override
	protected void onUpdate(float deltaTime) {
		super.onUpdate(deltaTime);
		
		if(Input.isMouseButtonDown(0)) {
			this.isOn = !this.isOn;
			//this.onToggle.invoke(this.isOn);
		}
	}
	
	public boolean isOn() {
		return this.isOn;
	}

	public void setOn(boolean on) {		
		if(!on && this.isOn) {
			if(this.toggleGroup != null && !this.toggleGroup.allowAllOff) {
				return; // Can't turn off the only on toggle.
			}
		} 
		
		this.isOn = on;
		
		if(on) {
			if(this.toggleGroup != null) {
				for(Toggle toggle : this.toggleGroup.getToggles()) {
					if(toggle == null || toggle == this) {
						continue;
					}
					
					toggle.isOn = false;
				}
			}
		}
	}
	
	public ToggleGroup getToggleGroup() {
		return this.toggleGroup;
	}
	
	public void setToggleGroup(ToggleGroup group) {
		if(this.toggleGroup == group) {
			return;
		}
		
		if(this.toggleGroup != null) { 
			this.toggleGroup.toggles.remove(this);
		}
		
		this.toggleGroup = group;
		
		if(group != null) {
			group.toggles.add(this);
		}
	}
}
