package com.codeshaper.jello.engine.test;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.codeshaper.jello.editor.GizmoDrawer;
import com.codeshaper.jello.editor.property.modifier.Button;
import com.codeshaper.jello.editor.property.modifier.DisableIf;
import com.codeshaper.jello.editor.property.modifier.DisplayAs;
import com.codeshaper.jello.editor.property.modifier.DontExposeField;
import com.codeshaper.jello.editor.property.modifier.ExposeField;
import com.codeshaper.jello.editor.property.modifier.HideIf;
import com.codeshaper.jello.editor.property.modifier.MinValue;
import com.codeshaper.jello.editor.property.modifier.ReadOnly;
import com.codeshaper.jello.editor.property.modifier.Separator;
import com.codeshaper.jello.editor.property.modifier.Space;
import com.codeshaper.jello.editor.property.modifier.ToolTip;
import com.codeshaper.jello.engine.Color;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.JelloComponent;
import com.codeshaper.jello.engine.Vector;
import com.codeshaper.jello.engine.asset.Mesh;

public class TestComponent extends JelloComponent {

	public boolean printCallbackTraces = true;

	@Space

	public int[] intArray = new int[] { 1, 2, 3 };
	@ToolTip("Tooltipppppp!")
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
	@ReadOnly
	public Vector3f vector = new Vector3f(1.05f, 2.1f, 3.2f);
	@Separator
	public Mesh mesh = null;
	@ExposeField
	private int exposed;
	@DontExposeField
	public int hidden;

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
