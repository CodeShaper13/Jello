package com.codeshaper.jello.engine.rendering;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform2f;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUniform4f;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL30.*;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.Utils;
import com.codeshaper.jello.engine.rendering.ShaderData.ShaderSource;

public class ShaderProgram {

	public final int programId;
	
	private Map<String, Integer> uniforms;
	
	private ShaderProgram() {
        this.programId = glCreateProgram();
        if (this.programId == 0) {
            throw new RuntimeException("Could not create Shader Program");
        }
        
		this.uniforms = new HashMap<>();
	}

    public ShaderProgram(ShaderModuleData... shaderModuleDataList) {
        this();

        List<Integer> shaderModules = new ArrayList<>();
        for(int i = 0; i < shaderModuleDataList.length; i++) {
        	ShaderModuleData data = shaderModuleDataList[i];
        	if(data != null) {
        		String code = Utils.readFile(data.shaderFile);
        		shaderModules.add(createShader(code, data.shaderType));
        	}
        }
        
        link(shaderModules);
    }
    
    public ShaderProgram(ShaderSource[] modules) {
        this();

        List<Integer> shaderModules = new ArrayList<>();
        for(int i = 0; i < modules.length; i++) {
        	ShaderSource data = modules[i];
        	if(data != null) {
        		shaderModules.add(createShader(data.getSource(), data.getType().type));
        	}
        }
        
        link(shaderModules);
    }

    public void bind() {
        glUseProgram(programId);
    }
    
    public void unbind() {
        glUseProgram(0);
    }

    public void createUniform(String uniformName) {
		int uniformLocation = glGetUniformLocation(this.programId, uniformName);
		if (uniformLocation < 0) {
			Debug.logError("Could not find uniform [%s] in shader program [%s]", uniformName, this.programId);
		}
		this.uniforms.put(uniformName, uniformLocation);
	}
    
    public void setUniform(String uniformName, int value) {
		glUniform1i(this.getUniformLocation(uniformName), value);
	}

	public void setUniform(String uniformName, float value) {
		glUniform1f(this.getUniformLocation(uniformName), value);
	}
	
    public void setUniform(String uniformName, Vector2f value) {
        glUniform2f(this.getUniformLocation(uniformName), value.x, value.y);
    }
	
    public void setUniform(String uniformName, Vector3f value) {
        glUniform3f(this.getUniformLocation(uniformName), value.x, value.y, value.z);
    }

    public void setUniform(String uniformName, Vector4f value) {
        glUniform4f(this.getUniformLocation(uniformName), value.x, value.y, value.z, value.w);
    }

	public void setUniform(String uniformName, Matrix4f value) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			glUniformMatrix4fv(this.getUniformLocation(uniformName), false, value.get(stack.mallocFloat(16)));
		}
	}
    
    public void cleanup() {
        unbind();
        if (programId != 0) {
            glDeleteProgram(programId);
        }
    }

    private int createShader(String shaderCode, int shaderType) {
        int shaderId = glCreateShader(shaderType);
        if (shaderId == 0) {
        	System.out.println("Error creating shader. Type: " + shaderType);
        }

        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
        	System.out.println("Error compiling Shader code: " + glGetShaderInfoLog(shaderId, 1024));
        }

        glAttachShader(programId, shaderId);

        return shaderId;
    }

    private void link(List<Integer> shaderModules) {
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            System.out.println("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));
        }

        shaderModules.forEach(s -> glDetachShader(programId, s));
        shaderModules.forEach(GL30::glDeleteShader);
    }

    private int getUniformLocation(String uniformName) {
		Integer location = this.uniforms.get(uniformName);
		if (location == null) {
			System.out.println("Could not find uniform [" + uniformName + "]");
		}
		return location.intValue();
	}
    
    public void validate() {
        glValidateProgram(programId);
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
        	System.out.println("Error validating Shader code: " + glGetProgramInfoLog(programId, 1024));
        }
    }
    
    public static class ShaderModuleData {
    	
    	public final int shaderType;
    	public final Path shaderFile;
    	
    	public static ShaderModuleData fromAssets(Path path, int shaderType) {
    		return new ShaderModuleData(path, shaderType);
    	}
    	
    	public static ShaderModuleData fromResources(String resourceLocation, int shaderType) {
    		Path path = null;
    		try {
    			URL url = ShaderModuleData.class.getResource(resourceLocation);
    			path = Paths.get(url.toURI());
    			return new ShaderModuleData(path, shaderType);
    		} catch (URISyntaxException e) {
    			e.printStackTrace();
    		}
    		
    		return null;
    	}
    	
    	private ShaderModuleData(Path shaderFile, int shaderType) {
    		this.shaderFile = shaderFile;
    		this.shaderType = shaderType;
       	}
    }
}
