package com.codeshaper.jello.engine.asset;

import static org.lwjgl.opengl.GL30.*;

import java.util.HashMap;
import java.util.Map.Entry;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.codeshaper.jello.editor.GuiLayoutBuilder;
import com.codeshaper.jello.editor.inspector.Editor;
import com.codeshaper.jello.editor.property.modifier.CreateAssetEntry;
import com.codeshaper.jello.engine.AssetLocation;
import com.codeshaper.jello.engine.Color;
import com.codeshaper.jello.engine.database.AssetDatabase;
import com.codeshaper.jello.engine.rendering.ShaderData;
import com.codeshaper.jello.engine.rendering.ShaderData.Property;
import com.codeshaper.jello.engine.rendering.ShaderProgram;

@CreateAssetEntry(fileName = "material", location = "Material")
public class Material extends SerializedJelloObject {

	private Shader shader = null;
	private HashMap<String, Integer> ints = new HashMap<String, Integer>();
	private HashMap<String, Float> floats = new HashMap<String, Float>();
	private HashMap<String, Vector2f> vec2s = new HashMap<String, Vector2f>();
	private HashMap<String, Vector3f> vec3s = new HashMap<String, Vector3f>();
	private HashMap<String, Vector4f> vec4s = new HashMap<String, Vector4f>();
	private HashMap<String, Texture> textures = new HashMap<String, Texture>();

	public Material(AssetLocation location) {
		super(location);
	}

	@Override
	public void onDeserialize() {
		super.onDeserialize();
		
		if(this.shader != null) {
			this.setShader(this.shader);
		}
	}

	@Override
	public Editor<?> getInspectorDrawer() {
		return new MaterialEditor(this);
	}

	/**
	 * Gets the Material's Shader. If no Shader is set, Jello's default shader is
	 * returned.
	 * 
	 * @return
	 */
	public Shader getShader() {
		if (this.shader == null) {
			return (Shader) AssetDatabase.getInstance().getAsset("builtin/shaders/default.shader");
		} else {
			return this.shader;
		}
	}

	public void setShader(Shader newShader) {
		if (newShader != this.shader) {
			// Shader has changed.
			this.ints.clear();
			this.floats.clear();
			this.vec2s.clear();
			this.vec3s.clear();
			this.vec4s.clear();
			this.textures.clear();
		}

		this.shader = newShader;

		if (newShader != null) {
			ShaderData data = this.shader.getData();
			ShaderProgram program = this.shader.getProgram();
			for (Property property : data.properties) {
				String uniform = property.uniform;
				if (program.doesUniformExist(uniform)) {
					switch (property.type) {
					case INT:
						if(!this.ints.containsKey(uniform)) {
							this.ints.put(uniform, property.getAsInt());
						}
						break;
					case FLOAT:
						if(!this.floats.containsKey(uniform)) {
							this.floats.put(uniform, property.getAsFloat());
						}
						break;
					case COLOR:
						if(!this.vec4s.containsKey(uniform)) {
							this.vec4s.put(uniform, property.getAsVec4f());
						}
						break;
					case VEC_2:
						if(!this.vec2s.containsKey(uniform)) {
							this.vec2s.put(uniform, property.getAsVec2f());
						}
						break;
					case VEC_3:
						if(!this.vec3s.containsKey(uniform)) {
							this.vec3s.put(uniform, property.getAsVec3f());
						}
						break;
					case VEC_4:
						if(!this.vec4s.containsKey(uniform)) {
							this.vec4s.put(uniform, property.getAsVec4f());
						}
						break;
					case TEXTURE:
						if(!this.textures.containsKey(uniform)) {
							this.textures.put(uniform, null);
						}
						break;
					}
				}
			}
		}
	}

	public int getInt(String uniform) {
		Integer i = this.ints.get(uniform);
		return i == null ? 0 : i;
	}

	public void setInt(String uniform, int value) {
		if (!this.doesUniformExist(uniform)) {
			return;
		}
		this.ints.put(uniform, value);
	}

	public float getFloat(String uniform) {
		Float f = this.floats.get(uniform);
		return f == null ? 0 : f;
	}

	public void setFloat(String uniform, float value) {
		if (!this.doesUniformExist(uniform)) {
			return;
		}
		this.floats.put(uniform, value);
	}

	public Color getColor(String uniform) {
		return new Color(this.getVector4f(uniform));
	}

	public void setColor(String uniform, Color value) {
		this.setVector4f(uniform, value.toVector4f());
	}

	public Vector2f getVector2f(String uniform) {
		Vector2f vec = this.vec2s.get(uniform);
		return vec == null ? new Vector2f(0, 0) : vec;
	}

	public void setVector2f(String uniform, Vector2f value) {
		if (value == null) {
			throw new IllegalArgumentException("value may not be null");
		}
		if (!this.doesUniformExist(uniform)) {
			return;
		}
		this.vec2s.put(uniform, value);
	}

	public Vector3f getVector3f(String uniform) {
		Vector3f vec = this.vec3s.get(uniform);
		return vec == null ? new Vector3f(0, 0, 0) : vec;
	}

	public void setVector3f(String uniform, Vector3f value) {
		if (value == null) {
			throw new IllegalArgumentException("value may not be null");
		}
		if (!this.doesUniformExist(uniform)) {
			return;
		}
		this.vec3s.put(uniform, value);
	}

	public Vector4f getVector4f(String uniform) {
		Vector4f vec = this.vec4s.get(uniform);
		return vec == null ? new Vector4f(0, 0, 0, 0) : vec;
	}

	public void setVector4f(String uniform, Vector4f value) {
		if (value == null) {
			throw new IllegalArgumentException("value may not be null");
		}
		if (!this.doesUniformExist(uniform)) {
			return;
		}
		this.vec4s.put(uniform, value);
	}

	public Texture getTexture(String uniform) {
		return this.textures.get(uniform);
	}

	public void setTexture(String uniform, Texture value) {
		if (!this.doesUniformExist(uniform)) {
			return;
		}
		this.textures.put(uniform, value);
	}

	public boolean doesUniformExist(String uniform) {
		if (this.shader == null) {
			return false;
		} else {
			return this.shader.getProgram().doesUniformExist(uniform);
		}
	}

	public void setUniforms() {
		if (this.shader == null) {
			return;
		}

		ShaderProgram program = this.shader.getProgram();
		for (Entry<String, Integer> entry : this.ints.entrySet()) {
			program.setUniform(entry.getKey(), entry.getValue());
		}
		for (Entry<String, Float> entry : this.floats.entrySet()) {
			program.setUniform(entry.getKey(), entry.getValue());
		}
		for (Entry<String, Vector2f> entry : this.vec2s.entrySet()) {
			program.setUniform(entry.getKey(), entry.getValue());
		}
		for (Entry<String, Vector3f> entry : this.vec3s.entrySet()) {
			program.setUniform(entry.getKey(), entry.getValue());
		}
		for (Entry<String, Vector4f> entry : this.vec4s.entrySet()) {
			program.setUniform(entry.getKey(), entry.getValue());
		}

		int counter = 0;
		for (Entry<String, Texture> entry : this.textures.entrySet()) {
			glActiveTexture(GL_TEXTURE0 + counter);
			Texture texture = entry.getValue();
			if (texture != null) {
				texture.bind();
			} else {
				glBindTexture(GL_TEXTURE_2D, 0);
			}
		}
	}

	public class MaterialEditor extends SerializedJelloObjectEditor<Material> {

		public MaterialEditor(Material target) {
			super(target);
		}

		@Override
		public void drawAsset(GuiLayoutBuilder builder) {			
			builder.assetField("Shader: ", this.target.shader, Shader.class, (v) -> {
				this.target.setShader(v);
				// TODO redraw editor.
			});
			
			builder.space();
			builder.label("Uniforms:");

			Shader shader = this.target.shader;
			if (shader != null) {
				ShaderData data = shader.getData();
				for (Property property : data.properties) {
					if (property.isValid()) {
						String fieldLabel = property.getDisplayName();
						String uniformName = property.uniform;
						switch (property.type) {
						case INT:
							builder.intField(fieldLabel, this.target.getInt(uniformName), (v) -> {
								this.target.setInt(uniformName, v);
							});
							break;
						case FLOAT:
							builder.floatField(fieldLabel, this.target.getFloat(uniformName), (v) -> {
								this.target.setFloat(uniformName, v);
							});
							break;
						case COLOR:
							builder.colorField(fieldLabel, this.target.getColor(uniformName), (v) -> {
								this.target.setColor(uniformName, v);
							});
							break;
						case VEC_2:
							builder.vector2fField(fieldLabel, this.target.getVector2f(uniformName), (v) -> {
								this.target.setVector2f(uniformName, v);
							});
							break;
						case VEC_3:
							builder.vector3fField(fieldLabel, this.target.getVector3f(uniformName), (v) -> {
								this.target.setVector3f(uniformName, v);
							});
							break;
						case VEC_4:
							builder.vector4fField(fieldLabel, this.target.getVector4f(uniformName), (v) -> {
								this.target.setVector4f(uniformName, v);
							});
							break;
						case TEXTURE:
							builder.assetField(fieldLabel, this.target.getTexture(uniformName), Texture.class, (v) -> {
								this.target.setTexture(uniformName, v);
							});
							break;
						default:
							break;
						}
					}
				}
			}
		}
	}
}
