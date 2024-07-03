package com.codeshaper.jello.engine.component;

import static org.lwjgl.opengl.GL11.*;

import org.joml.Vector3d;

import com.codeshaper.jello.editor.property.modifier.Button;
import com.codeshaper.jello.editor.property.modifier.DisableIf;
import com.codeshaper.jello.editor.property.modifier.DisplayAs;
import com.codeshaper.jello.editor.property.modifier.HideIf;
import com.codeshaper.jello.editor.property.modifier.MinValue;
import com.codeshaper.jello.editor.property.modifier.ReadOnly;
import com.codeshaper.jello.editor.property.modifier.Separator;
import com.codeshaper.jello.editor.property.modifier.Space;
import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.asset.Mesh;

public class Test extends JelloComponent {
	
	public int[] intArray = new int[] { 1, 2, 3 };
	public String text = "Hello World";
	public byte byte1 = 3;
	@MinValue(-2)
	public int integer = 14;
	@HideIf("hideIfFunc")
	public int hiddenInt = 999;
	@DisableIf("disableIfFunc")
	public int disabledInt = 999;
	public Integer integerWrapper = 10;
	@Space(14)
	public short short1;
	public long long1 = -12345;
	@ReadOnly
	public float float1 = 0.5f;
	public double double1 = 123.456789;
	@DisplayAs("My Boolean")
	public boolean bool = true;
	public EnumTest testEnum = EnumTest.BANANA;
	public Vector3d vector = new Vector3d(1.05, 2.1, 3.2);
	@Separator
	public Mesh mesh = null;

	public Test(GameObject owner) {
		super(owner);
	}
	
	@Override
	public void onRender() {		
		float size = 0.1f;

    	glBegin(GL_QUADS);
    	
    	// Top (+y).
    	glColor3f(0,  1,  0);
    	glVertex3f(-size, size, -size);
    	glVertex3f(-size, size, size);
    	glVertex3f(size, size, size);
    	glVertex3f(size, size, -size);

    	// Bottom (+y).
    	glColor3f(0,  1,  0);
    	glVertex3f(size, -size, -size);
    	glVertex3f(size, -size, size);
    	glVertex3f(-size, -size, size);
    	glVertex3f(-size, -size, -size);
    	
    	// Front (+z).
    	glColor3f(0,  0,  1);
    	glVertex3f(size, -size, size);
    	glVertex3f(size, size, size);
    	glVertex3f(-size, size, size);
    	glVertex3f(-size, -size, size);
    	
    	// Back (+z).
    	glColor3f(0,  0,  1);
    	glVertex3f(-size, -size, -size);
    	glVertex3f(-size, size, -size);
    	glVertex3f(size, size, -size);
    	glVertex3f(size, -size, -size);
    	
    	// (+x).
    	glColor3f(1f, 0f, 0f);
    	glVertex3f(size, size, -size);
    	glVertex3f(size, size, size);
    	glVertex3f(size, -size, size);
    	glVertex3f(size, -size, -size);
    	
    	// (-x).
    	glColor3f(1f, 0f, 0f);
    	glVertex3f(-size, -size, -size);
    	glVertex3f(-size,-size, size);
    	glVertex3f(-size,size, size);
    	glVertex3f(-size,size, -size);
    	
    	glEnd();
   	}
	
	@Button
	private void Function1() {
		System.out.println("1");
	}
	
	@Button("Function 2")
	public void Func2() {
		System.out.println("2");
	}
	
	@SuppressWarnings("unused")
	private boolean hideIfFunc() {
		return true;
	}

	@SuppressWarnings("unused")
	private boolean disableIfFunc() {
		return true;
	}
	
	public enum EnumTest {
		APPLE,
		BANANA,
		ORANGE,
	}
}
