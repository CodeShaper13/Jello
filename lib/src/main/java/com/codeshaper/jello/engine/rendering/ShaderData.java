package com.codeshaper.jello.engine.rendering;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.SerializedName;

public class ShaderData {

	public Property[] properties = new Property[0];
	public ShaderSource[] shaders = new ShaderSource[] {};

	public class Property {

		public final String displayAs;
		/**
		 * The name of the uniform this property represents.
		 */
		public final String uniform;
		/**
		 * The type of the uniform this property represents.
		 */
		public final UniformType type;
		/**
		 * The default value of this uniform.
		 */
		public final Object value;

		public Property() {
			this.displayAs = null;
			this.uniform = null;
			this.type = null;
			this.value = null;
		}

		public int getAsInt() {
			if (this.value instanceof Integer) {
				return (Integer) this.value;
			} else {
				return 0;
			}
		}

		public float getAsFloat() {
			if (this.value instanceof Double) {
				return ((Double)this.value).floatValue();
			} else {
				return 0;
			}
		}

		public Vector2f getAsVec2f() {
			Vector2f vec = new Vector2f();
			for (int i = 0; i < 3; i++) {
				vec.setComponent(i, this.tryGetFloat(this.value, i));
			}
			return vec;
		}

		public Vector3f getAsVec3f() {
			Vector3f vec = new Vector3f();
			for (int i = 0; i < 3; i++) {
				vec.setComponent(i, this.tryGetFloat(this.value, i));
			}
			return vec;
		}

		public Vector4f getAsVec4f() {
			Vector4f vec = new Vector4f();
			for (int i = 0; i < 4; i++) {
				vec.setComponent(i, this.tryGetFloat(this.value, i));
			}
			return vec;
		}

		/**
		 * Checks if this property is fully defined, thus a valid property. For a
		 * property to be valid it must define a "uniformName" and "type".
		 * 
		 * @return {@code true} if the property is valid.
		 */
		public boolean isValid() {
			return this.uniform != null && this.type != null;
		}

		/**
		 * Gets the display name of the property to show in the inspector.
		 * 
		 * @return the name of the property to show in the inspector.
		 */
		public String getDisplayName() {
			if (this.displayAs != null) {
				return this.displayAs;
			}
			return this.uniform;
		}

		/**
		 * @deprecated does not work, always returns 0
		 */
		@Deprecated()
		private float tryGetFloat(Object array, int index) {
			if(array instanceof JsonObject) {
				JsonObject obj = (JsonObject)array;
			}
			if (array instanceof JsonArray) {
				System.out.println("2");
				JsonArray jArray = (JsonArray) array;
				if (index < jArray.size()) {
					JsonElement element = jArray.get(index);
					if (element instanceof JsonPrimitive) {
						JsonPrimitive primitive = (JsonPrimitive) element;
						if (primitive.isNumber()) {
							return primitive.getAsFloat();
						}
					}
				}
			}
			return 0f;
		}

		public enum UniformType {
			@SerializedName("int")
			INT,
			@SerializedName("float")
			FLOAT,
			@SerializedName("color")
			COLOR,
			@SerializedName("vec2")
			VEC_2,
			@SerializedName("vec3")
			VEC_3,
			@SerializedName("vec4")
			VEC_4,
			@SerializedName("texture")
			TEXTURE,
		}
	}
}
