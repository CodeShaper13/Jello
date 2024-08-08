package com.codeshaper.jello.engine;

import java.util.Objects;

import org.joml.Math;
import org.joml.Vector4f;

public class Color {

	public static Color red = new Color(1f, 0f, 0f);
	public static Color green = new Color(0f, 1f, 0f);
	public static Color blue = new Color(0f, 0f, 1f);
	public static Color yellow = new Color(1f, 1f, 0f);
	public static Color magenta = new Color(1f, 0f, 1f);
	public static Color cyan = new Color(0f, 1f, 1f);
	public static Color white = new Color(1f, 1f, 1f);
	public static Color gray = new Color(0.5f, 0.5f, 0.5f);
	public static Color black = new Color(0f, 0f, 0f);
	public static Color pink = new Color(1f, 0.5f, 1f);
	public static Color brown = new Color(0.5f, 0.25f, 0.1f);
	public static Color clear = new Color(1f, 1f, 1f, 0f);

	public final float r;
	public final float g;
	public final float b;
	public final float a;

	public Color(float r, float g, float b) {
		this(r, g, b, 1f);
	}

	public Color(float r, float g, float b, float a) {
		this.r = Math.clamp(0f, 1f, r);
		this.g = Math.clamp(0f, 1f, g);
		this.b = Math.clamp(0f, 1f, b);
		this.a = Math.clamp(0f, 1f, a);
	}

	public Color(String hex) {
		// Remove a possible starting "#'.
		hex = hex.replace("#", "");
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
		}
		throw new IllegalArgumentException();
	}

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

	public Vector4f toVector4f() {
		return new Vector4f(this.r, this.g, this.b, this.a);
	}

	@Override
	public String toString() {
		return String.format("Color[r={0}, g={1}, b={2}, a={3}", this.r, this.g, this.b, this.a);
	}

	@Override
	public int hashCode() {
		return Objects.hash(a, b, g, r);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
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