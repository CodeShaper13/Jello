package com.codeshaper.jello.engine.test;

import org.joml.Vector2i;

import com.codeshaper.jello.editor.property.modifier.Button;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.CursorState;
import com.codeshaper.jello.engine.Input;
import com.codeshaper.jello.engine.JelloComponent;
import com.codeshaper.jello.engine.KeyCode;

public class InputTest extends JelloComponent {

	public boolean printMousePos;
	public CursorState cursorMode = CursorState.NORMAL;
	
	@Override
	protected void onUpdate(float deltaTime) {
		/////////////////
		// Mouse Tests //
		/////////////////
		if(this.printMousePos) {
			Vector2i pos = Input.getMousePos();
			Debug.log("Mouse Position: (%s, %s)", pos.x, pos.y);
		}
		
		int scroll = Input.getMouseScroll();
		if(scroll != 0) {
			Debug.log("Mouse Scroll: " + scroll);			
		}
		
		if(Input.isMouseButtonPressed(0)) {
			Debug.log("Left Mouse Button Pressed");
		}
		if(Input.isMouseButtonReleased(0)) {
			Debug.log("Left Mouse Button Released");
		}
		
		
		////////////////////
		// Keyboard Tests //
		////////////////////
		if(Input.isKeyPressed(KeyCode.SPACE)) {
			Debug.log("Space Key Pressed!");
		}
		if(Input.isKeyReleased(KeyCode.SPACE)) {
			Debug.log("Space Key Released!");
		}
	}
	
	@Button
	private void updateCursorMode() {
		Input.setCursorState(this.cursorMode);
	}
}
