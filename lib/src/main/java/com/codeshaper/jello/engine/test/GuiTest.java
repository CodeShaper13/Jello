package com.codeshaper.jello.engine.test;

import org.joml.Vector2f;

public class GuiTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Vector2f screenSize = new Vector2f(800, 600);
		
		Vector2f uiPos =new Vector2f(250, 50);
		
		
		// Take normalized positions (from 0-1) and change it to screen space (
		float w = (1f / screenSize.x) * uiPos.x;
		
		System.out.println(w);
	}
}
