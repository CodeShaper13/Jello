package com.codeshaper.jello.engine.asset;

import static org.lwjgl.opengl.GL30.*;

import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JPanel;

import org.apache.commons.lang3.StringUtils;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.codeshaper.jello.editor.EditorUtils;
import com.codeshaper.jello.editor.GuiLayoutBuilder;
import com.codeshaper.jello.editor.inspector.Editor;
import com.codeshaper.jello.editor.property.modifier.CreateAssetEntry;
import com.codeshaper.jello.engine.AssetLocation;
import com.codeshaper.jello.engine.Color;
import com.codeshaper.jello.engine.rendering.shader.ShaderProgram;
import com.codeshaper.jello.engine.rendering.shader.Uniform;
import com.codeshaper.jello.engine.rendering.shader.UniformType;

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

		if (this.shader != null) {
			this.setShader(this.shader);
		}
	}

	@Override
	public Editor<?> getInspectorDrawer(JPanel panel) {
		return new MaterialEditor(this, panel);
	}

	/**
	 * Gets the Material's Shader. If no Shader is set, null is returned.
	 * 
	 * @return the Material's Shader.
	 */
	public Shader getShader() {
		return this.shader;
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

		if (newShader != null && !newShader.isInvalid()) {
			for (Uniform uniform : newShader.getProgram().getAllUniforms()) {
				String uniformName = uniform.name;
				switch (uniform.type) {
				case int_:
					if (!this.ints.containsKey(uniformName)) {
						this.ints.put(uniformName, 0);
					}
					break;
				case float_:
					if (!this.floats.containsKey(uniformName)) {
						this.floats.put(uniformName, 0f);
					}
					break;
				case vec2:
					if (!this.vec2s.containsKey(uniformName)) {
						this.vec2s.put(uniformName, new Vector2f());
					}
					break;
				case vec3:
					if (!this.vec3s.containsKey(uniformName)) {
						this.vec3s.put(uniformName, new Vector3f());
					}
					break;
				case vec4:
					if (!this.vec4s.containsKey(uniformName)) {
						Vector4f vec;
						if (this.shouldTreatUniformAsColor(uniform)) {
							vec = new Vector4f(1f, 1f, 1f, 1f);
						} else {
							vec = new Vector4f(0f, 0f, 0f, 0f);
						}
						this.vec4s.put(uniformName, vec);
					}
					break;
				case sampler2D:
					if (!this.textures.containsKey(uniformName)) {
						this.textures.put(uniformName, null);
					}
					break;
				default:
					break;
				}
			}
		}
	}

	/**
	 * Gets the value of a uniform. 0 is returned on error. An error is caused if no
	 * shader is set, the shader does not have a uniform with the passed name, or
	 * the uniform is not of type int.
	 * 
	 * @param uniform the uniform name.
	 * @return the value of the uniform.
	 */
	public int getInt(String uniform) {
		Integer i = this.ints.get(uniform);
		return i == null ? 0 : i;
	}

	/**
	 * Sets the value of an int uniform. If the uniform does not exist, nothing
	 * happens.
	 * 
	 * @param uniform the name of the uniform.
	 * @param value   the new value.
	 */
	public void setInt(String uniform, int value) {
		if (!this.doesUniformExist(uniform)) {
			return;
		}
		this.ints.put(uniform, value);
	}

	/**
	 * Gets the value of a uniform. 0 is returned on error. An error is caused if no
	 * shader is set, the shader does not have a uniform with the passed name, or
	 * the uniform is not of type float.
	 * 
	 * @param uniform the name of the uniform.
	 * @return the value of the uniform.
	 */
	public float getFloat(String uniform) {
		Float f = this.floats.get(uniform);
		return f == null ? 0 : f;
	}

	/**
	 * Sets the value of a float uniform. If the uniform does not exist, nothing
	 * happens.
	 * 
	 * @param uniform the name of the uniform.
	 * @param value   the new value.
	 */
	public void setFloat(String uniform, float value) {
		if (!this.doesUniformExist(uniform)) {
			return;
		}
		this.floats.put(uniform, value);
	}

	/**
	 * Gets the value of a uniform. 0 is returned on error. An error is caused if no
	 * shader is set, the shader does not have a uniform with the passed name, or
	 * the uniform is not of type vec4.
	 * 
	 * @param uniform the name of the uniform.
	 * @return the value of the uniform.
	 */
	public Color getColor(String uniform) {
		return new Color(this.getVec4(uniform));
	}

	/**
	 * Sets the value of a vec4 uniform. If the uniform does not exist, nothing
	 * happens.
	 * 
	 * @param uniform the name of the uniform.
	 * @param value   the new value.
	 * @throws IllegalArgumentException if {@code value} is null.
	 */
	public void setColor(String uniform, Color value) {
		this.setVec4(uniform, value.toVector4f());
	}

	/**
	 * Gets the value of a uniform. 0 is returned on error. An error is caused if no
	 * shader is set, the shader does not have a uniform with the passed name, or
	 * the uniform is not of type vec2.
	 * 
	 * @param uniform the name of the uniform.
	 * @return the value of the uniform.
	 */
	public Vector2f getVec2(String uniform) {
		Vector2f vec = this.vec2s.get(uniform);
		return vec == null ? new Vector2f(0, 0) : vec;
	}

	/**
	 * Sets the value of a vec2 uniform. If the uniform does not exist, nothing
	 * happens.
	 * 
	 * @param uniform the name of the uniform.
	 * @param value   the new value.
	 * @throws IllegalArgumentException if {@code value} is null.
	 */
	public void setVec2(String uniform, Vector2f value) {
		if (value == null) {
			throw new IllegalArgumentException("value may not be null");
		}
		if (!this.doesUniformExist(uniform)) {
			return;
		}
		this.vec2s.put(uniform, value);
	}

	/**
	 * Gets the value of a uniform. 0 is returned on error. An error is caused if no
	 * shader is set, the shader does not have a uniform with the passed name, or
	 * the uniform is not of type vec3.
	 * 
	 * @param uniform the name of the uniform.
	 * @return the value of the uniform.
	 */
	public Vector3f getVec3(String uniform) {
		Vector3f vec = this.vec3s.get(uniform);
		return vec == null ? new Vector3f(0, 0, 0) : vec;
	}

	/**
	 * Sets the value of a vec3 uniform. If the uniform does not exist, nothing
	 * happens.
	 * 
	 * @param uniform the name of the uniform.
	 * @param value   the new value.
	 * @throws IllegalArgumentException if {@code value} is null.
	 */
	public void setVec3(String uniform, Vector3f value) {
		if (value == null) {
			throw new IllegalArgumentException("value may not be null");
		}
		if (!this.doesUniformExist(uniform)) {
			return;
		}
		this.vec3s.put(uniform, value);
	}

	/**
	 * Gets the value of a uniform. 0 is returned on error. An error is caused if no
	 * shader is set, the shader does not have a uniform with the passed name, or
	 * the uniform is not of type vec4.
	 * 
	 * @param uniform the name of the uniform.
	 * @return the value of the uniform.
	 */
	public Vector4f getVec4(String uniform) {
		Vector4f vec = this.vec4s.get(uniform);
		return vec == null ? new Vector4f(0, 0, 0, 0) : vec;
	}

	/**
	 * Sets the value of a vec4 uniform. If the uniform does not exist, nothing
	 * happens.
	 * 
	 * @param uniform the name of the uniform.
	 * @param value   the new value.
	 * @throws IllegalArgumentException if {@code value} is null.
	 */
	public void setVec4(String uniform, Vector4f value) {
		if (value == null) {
			throw new IllegalArgumentException("value may not be null");
		}
		if (!this.doesUniformExist(uniform)) {
			return;
		}
		this.vec4s.put(uniform, value);
	}

	/**
	 * Gets the value of a uniform. 0 is returned on error. An error is caused if no
	 * shader is set, the shader does not have a uniform with the passed name, or
	 * the uniform is not of type sampler2D.
	 * 
	 * @param uniform the name of the uniform.
	 * @return the value of the uniform.
	 */
	public Texture getTexture(String uniform) {
		return this.textures.get(uniform);
	}

	/**
	 * Sets the value of a sampler2d uniform. If the uniform does not exist, nothing
	 * happens.
	 * 
	 * @param uniform the name of the uniform.
	 * @param value   the new value.
	 * @throws IllegalArgumentException if {@code value} is null.
	 */
	public void setTexture(String uniform, Texture value) {
		if (!this.doesUniformExist(uniform)) {
			return;
		}
		this.textures.put(uniform, value);
	}

	/**
	 * Checks if a uniform exists.
	 * 
	 * @param uniform the name of the uniform.
	 * @return {@code true} if the uniform exists,
	 */
	public boolean doesUniformExist(String uniform) {
		if (this.shader == null) {
			return false;
		} else {
			return this.shader.getProgram().doesUniformExist(uniform);
		}
	}

	/**
	 * Sets the uniforms of this Material's Shader. If this Material has no Shader,
	 * or the Shader is invalid, nothing happens.
	 */
	public void setUniforms() {
		if (this.shader == null || this.shader.isInvalid()) {
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
			Texture texture = entry.getValue();
			glActiveTexture(GL_TEXTURE0 + counter);
			if (texture != null) {
				texture.bind();
			} else {
				glBindTexture(GL_TEXTURE_2D, 0);
			}
			counter++;
		}
	}

	private boolean shouldTreatUniformAsColor(Uniform uniform) {
		if (uniform.type != UniformType.vec4) {
			return false;
		} else {
			return StringUtils.containsIgnoreCase(uniform.name, "color");
		}
	}

	public class MaterialEditor extends SerializedJelloObjectEditor<Material> {

		public MaterialEditor(Material target, JPanel panel) {
			super(target, panel);
		}

		@Override
		public void drawAsset(GuiLayoutBuilder builder) {
			builder.assetField("Shader: ", this.target.shader, Shader.class, (v) -> {
				this.target.setShader(v);
				this.onRefresh();
			});

			builder.space();
			builder.label("Uniforms:");

			Shader shader = this.target.shader;
			if (shader != null && !shader.isInvalid()) {
				for (Uniform uniform : shader.getProgram().getAllUniforms()) {
					String uniformName = uniform.name;
					
					String fieldLabel = EditorUtils.formatName(uniformName);
					
					switch (uniform.type) {
					case int_:
						builder.intField(fieldLabel, this.target.getInt(uniformName), (v) -> {
							this.target.setInt(uniformName, v);
						});
						break;
					case float_:
						builder.floatField(fieldLabel, this.target.getFloat(uniformName), (v) -> {
							this.target.setFloat(uniformName, v);
						});
						break;
					case vec2:
						builder.vector2fField(fieldLabel, this.target.getVec2(uniformName), (v) -> {
							this.target.setVec2(uniformName, v);
						});
						break;
					case vec3:
						builder.vector3fField(fieldLabel, this.target.getVec3(uniformName), (v) -> {
							this.target.setVec3(uniformName, v);
						});
						break;
					case vec4:
						if(shouldTreatUniformAsColor(uniform) ) {
							builder.colorField(fieldLabel, this.target.getColor(uniformName), (v) -> {
								this.target.setColor(uniformName, v);
							});
						} else {
							builder.vector4fField(fieldLabel, this.target.getVec4(uniformName), (v) -> {
								this.target.setVec4(uniformName, v);
							});
						}
						break;
					case sampler2D:
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
