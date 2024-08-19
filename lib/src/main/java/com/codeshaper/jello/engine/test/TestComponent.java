package com.codeshaper.jello.engine.test;

import org.joml.Quaternionf;

import com.codeshaper.jello.editor.GizmoDrawer;
import com.codeshaper.jello.editor.property.modifier.Button;
import com.codeshaper.jello.editor.property.modifier.DisableIf;
import com.codeshaper.jello.editor.property.modifier.DisplayAs;
import com.codeshaper.jello.editor.property.modifier.DontExposeField;
import com.codeshaper.jello.editor.property.modifier.ExposeField;
import com.codeshaper.jello.editor.property.modifier.HideIf;
import com.codeshaper.jello.editor.property.modifier.MaxValue;
import com.codeshaper.jello.editor.property.modifier.MinValue;
import com.codeshaper.jello.editor.property.modifier.Range;
import com.codeshaper.jello.editor.property.modifier.ReadOnly;
import com.codeshaper.jello.editor.property.modifier.Separator;
import com.codeshaper.jello.editor.property.modifier.Space;
import com.codeshaper.jello.editor.property.modifier.TextBox;
import com.codeshaper.jello.editor.property.modifier.ToolTip;
import com.codeshaper.jello.engine.Color;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.JelloComponent;
import com.codeshaper.jello.engine.Vector;
import com.codeshaper.jello.engine.asset.Texture;

public class TestComponent extends JelloComponent {

	public boolean printCallbackTraces = true;

	@Space
	
	public byte myByte = 1;
	public int myInt = 2;
	public short myShort = 3;
	public long myLong = 4;
	public float myFloat = 5.6f;
	public double myDouble = 7.8;
	public boolean myBool = true;
	public Integer myIntegerWrapper = 9;
	public String myString = "Hello World";
	public EnumTest myEnum = EnumTest.BANANA;
	public int[] myArray = new int[] { 1, 2, 3 };
	public Color myColor = Color.red;
	public Texture myAssetReference = null;
	
	@Space

	@DisableIf("!myBool")
	public int disableIf0 = 1;
	@DisableIf("disableIfFunc()")
	public int disableIf = 1;
	@DisplayAs("Custom Name!")
	public int customName = 1;
	@DontExposeField
	public int hidden;
	@ExposeField
	private int myPrivateField;
	@HideIf("myBool")
	public int hideIf;
	@MaxValue(10)
	public int maxValue10 = 5;
	@MinValue(10)
	public int minValue0 = 5;
	@Range(min = 0, max = 10)
	public int range = 1;
	@ReadOnly
	public int readonly = 1;
	@Separator
	public int seperatorAbove = 1;
	@TextBox
	public String textbox = "Textbox";
	@ToolTip("An example of a tooltip")
	public int tooltip = 1;
	
	@Override
	public void onDrawGizmos(GizmoDrawer gizmos, boolean isSelected) {
		gizmos.color(Color.white.setA(0.5f));
		gizmos.drawCube(this.getOwner().getPosition(), new Quaternionf(), Vector.vector3Zero());
	}
	
	@Override
	public void onConstruct() {
		if (this.printCallbackTraces) {
			Debug.log("TestComponent#onConstruct()");
		}
	}

	@Override
	public void onStart() {
		if (this.printCallbackTraces) {
			Debug.log("TestComponent#onStart()");
		}
	}

	@Override
	public void onEnable() {
		Debug.log("TestComponent#onEnable()");
	}

	@Override
	public void onUpdate(float deltaTime) {
		if (this.printCallbackTraces) {
			Debug.log("TestComponent#onUpdate(%s)", deltaTime);
		}
	}

	@Override
	public void onDisable() {
		if (this.printCallbackTraces) {
			Debug.log("TestComponent#onDisable()");
		}
	}

	@Override
	public void onDestroy() {
		if (this.printCallbackTraces) {
			Debug.log("TestComponent#onDestroy()");
		}
	}

	@Button
	private void buttonFunction1() {
		System.out.println("1");
	}

	@Button("Button Function 2")
	public void btnFunc() {
		System.out.println("2");
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
