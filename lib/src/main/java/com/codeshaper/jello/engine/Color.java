package com.codeshaper.jello.engine;

import java.util.Objects;

import org.joml.Math;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Provides a way of representing colors in the RGBA color space. Colors are
 * defined with 4 float, ranging from 0 to 1, representing the red, green, blue
 * and alpha values of a color.
 * <p>
 * Colors are immutable objects. This is to prevent unexpected behavior when
 * modifying the components of a color. The {@link Color#setR(float)},
 * {@link Color#setR(float)}, {@link Color#setR(float)},
 * {@link Color#setR(float)}, while appearing to modify the color, they create a
 * color instance and returns it.
 */
public class Color {

	/**
	 * An regex expression that matches hexadecimal characters. 0-9, and a-f (upper
	 * and lower case).
	 */
	public static final String HEX_REGEX = "^[0-9A-Fa-f]+$";

	/**
	 * RGBA is (1, 0, 0, 1).
	 */
	public static Color red = new Color(1f, 0f, 0f);
	/**
	 * RGBA is (0, 1, 0, 1).
	 */
	public static Color green = new Color(0f, 1f, 0f);
	/**
	 * RGBA is (0, 0, 1, 1).
	 */
	public static Color blue = new Color(0f, 0f, 1f);
	/**
	 * RGBA is (1, 1, 1, 1).
	 */
	public static Color yellow = new Color(1f, 1f, 0f);
	/**
	 * RGBA is (1, 0, 1, 1).
	 */
	public static Color magenta = new Color(1f, 0f, 1f);
	/**
	 * RGBA is (0, 1, 1, 1).
	 */
	public static Color cyan = new Color(0f, 1f, 1f);
	/**
	 * RGBA is (1, 1, 1, 1).
	 */
	public static Color white = new Color(1f, 1f, 1f);
	/**
	 * RGBA is (0.5, 0.5, 0.5, 1).
	 */
	public static Color gray = new Color(0.5f, 0.5f, 0.5f);
	/**
	 * RGBA is (0, 0, 0, 1).
	 */
	public static Color black = new Color(0f, 0f, 0f);
	/**
	 * RGBA is (1, 0.5, 1, 1).
	 */
	public static Color pink = new Color(1f, 0.5f, 1f);
	/**
	 * RGBA is (0.5, 0.25, 0.1, 1).
	 */
	public static Color brown = new Color(0.5f, 0.25f, 0.1f);
	/**
	 * RGBA is (1, 1, 1, 0).
	 */
	public static Color clear = new Color(1f, 1f, 1f, 0f);

	/**
	 * The red component of the Color, ranging from 0 to 1.
	 */
	public final float r;
	/**
	 * The green component of the Color, ranging from 0 to 1.
	 */
	public final float g;
	/**
	 * The blue component of the Color, ranging from 0 to 1.
	 */
	public final float b;
	/**
	 * The alpha component of the Color, ranging from 0 to 1. 0 is completely
	 * transparent and 1 is completely opaque.
	 */
	public final float a;

	/**
	 * Creates a Color with the specified red, green and blue components with the
	 * alpha component set to 1.
	 * 
	 * @param r the red component, ranging from 0 to 1
	 * @param g the green component, ranging from 0 to 1
	 * @param b the blue component, ranging from 0 to 1
	 */
	public Color(float r, float g, float b) {
		this(r, g, b, 1f);
	}

	/**
	 * Creates a Color with the specified red, green, blue and alpha components.
	 * 
	 * @param r the red component, ranging from 0 to 1
	 * @param g the green component, ranging from 0 to 1
	 * @param b the blue component, ranging from 0 to 1
	 * @param a the alpha component, ranging from 0 to 1
	 */
	public Color(float r, float g, float b, float a) {
		this.r = Math.clamp(0f, 1f, r);
		this.g = Math.clamp(0f, 1f, g);
		this.b = Math.clamp(0f, 1f, b);
		this.a = Math.clamp(0f, 1f, a);
	}

	public Color(byte r, byte g, byte b) {
		this(r, g, b, 255);
	}

	public Color(byte r, byte g, byte b, byte a) {
		this((r & 0xFF) / 255f, (g & 0xFF) / 255f, (b & 0xFF) / 255f, (a & 0xFF) / 255f);
	}

	/**
	 * Creates a {@link Color} from a hexadecimal. {@code hex} must contain either 6
	 * characters, for the red, green and blue components, or 8 character for the
	 * red, green, blue and alpha components.
	 * 
	 * @param hex
	 */
	public Color(String hex) {
		if (!hex.matches(Color.HEX_REGEX)) {
			throw new IllegalArgumentException("hex can only contain the characters 0-9 and a-f");
		}

		int length = hex.length();
		if (length == 6) {
			this.r = Math.clamp(0f, 1f, Integer.valueOf(hex.substring(0, 2), 16) / 255f);
			this.g = Math.clamp(0f, 1f, Integer.valueOf(hex.substring(2, 4), 16) / 255f);
			this.b = Math.clamp(0f, 1f, Integer.valueOf(hex.substring(4, 6), 16) / 255f);
			this.a = 1f;
		} else if (length == 8) {
			this.r = Math.clamp(0f, 1f, Integer.valueOf(hex.substring(0, 2), 16) / 255f);
			this.g = Math.clamp(0f, 1f, Integer.valueOf(hex.substring(2, 4), 16) / 255f);
			this.b = Math.clamp(0f, 1f, Integer.valueOf(hex.substring(4, 6), 16) / 255f);
			this.a = Math.clamp(0f, 1f, Integer.valueOf(hex.substring(6, 8), 16) / 255f);
		} else {
			throw new IllegalArgumentException("hex must be either 6 or 8 characters");
		}
	}

	/**
	 * Creates a {@link Color} from a Java AWT Color.
	 * 
	 * @param color
	 */
	public Color(java.awt.Color color) {
		this(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
	}

	/**
	 * Creates a new color with {@link Vector4f#x} as the red component,
	 * {@link Vector4f#y} as the green component, {@link Vector4f#z} as the blue
	 * component and {@link Vector4f#w} as the alpha component.
	 * 
	 * @param vector the vector to create the color from.
	 */
	public Color(Vector4f vector) {
		this(vector.x, vector.y, vector.z, vector.w);
	}

	/**
	 * Gets one of the component from the color based on the index. This is useful
	 * if you want to iterate through each of the components. 0, 1, 2 and 3
	 * correspond with the red, green, blue and alpha components respectively.
	 * 
	 * @param index the index of the component to get
	 * @return the specified component
	 * @throws IllegalArgumentException if index is less than 0 or greater than 3
	 */
	public float getComponent(int index) {
		switch (index) {
		case 0:
			return this.r;
		case 1:
			return this.g;
		case 2:
			return this.b;
		case 3:
			return this.a;
		}
		throw new IllegalArgumentException("index must be between 0 and 3 inclusivly.");
	}

	public Color setR(float red) {
		return new Color(red, this.g, this.b);
	}

	public Color setG(float green) {
		return new Color(this.r, green, this.b);
	}

	public Color setB(float blue) {
		return new Color(this.r, this.g, blue);
	}

	public Color setA(float alpha) {
		return new Color(this.r, this.g, this.b, alpha);
	}

	public java.awt.Color toAwtColor() {
		return new java.awt.Color(this.r, this.g, this.b, this.a);
	}

	/**
	 * Creates a {@link Vector3f} from the red, green and blue components of the
	 * color as the x, y and z components of the Vector3f respectively.
	 * 
	 * @return the Color as a Vector3f
	 */
	public Vector3f toVector3f() {
		return new Vector3f(this.r, this.g, this.b);
	}

	/**
	 * Creates a {@link Vector4f} from the red, green, blue and alpha components of
	 * the color as the x, y, z and w components of the Vector4f respectively.
	 * 
	 * @return the Color as a Vector4f
	 */
	public Vector4f toVector4f() {
		return new Vector4f(this.r, this.g, this.b, this.a);
	}

	/**
	 * Returns "Color[r=RED, g=GREEN, b=BLUE, a=ALPHA] where RED is the red
	 * component as a float, GREEN is the green component as a float, BLUE is the
	 * blue component as a float, and ALPHA is the alpha component as a float.
	 */
	@Override
	public String toString() {
		return String.format("Color[r=%s, g=%s, b=%s, a=%s]", this.r, this.g, this.b, this.a);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.a, this.b, this.g, this.r);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		Color other = (Color) obj;
		return Float.floatToIntBits(a) == Float.floatToIntBits(other.a)
				&& Float.floatToIntBits(b) == Float.floatToIntBits(other.b)
				&& Float.floatToIntBits(g) == Float.floatToIntBits(other.g)
				&& Float.floatToIntBits(r) == Float.floatToIntBits(other.r);
	}

	public static Color lerp(Color color1, Color color2, float t) {
		t = Math.clamp(0f, 1f, t);
		float r = Math.fma(color2.r - color1.r, t, color1.r);
		float g = Math.fma(color2.g - color1.g, t, color1.g);
		float b = Math.fma(color2.b - color1.b, t, color1.b);
		float a = Math.fma(color2.a - color1.a, t, color1.a);
		return new Color(r, g, b, a);
	}
}