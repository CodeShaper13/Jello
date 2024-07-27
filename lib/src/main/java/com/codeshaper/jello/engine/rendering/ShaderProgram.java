package com.codeshaper.jello.engine.rendering;

import static org.lwjgl.opengl.GL30.*;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL30;

import com.codeshaper.jello.engine.Utils;
import com.codeshaper.jello.engine.rendering.ShaderData.ShaderSource;

public class ShaderProgram {

	private final int programId;
	
	private ShaderProgram() {
        this.programId = glCreateProgram();
        if (this.programId == 0) {
            throw new RuntimeException("Could not create Shader");
        }
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

    public void cleanup() {
        unbind();
        if (programId != 0) {
            glDeleteProgram(programId);
        }
    }

    protected int createShader(String shaderCode, int shaderType) {
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

    public int getProgramId() {
        return programId;
    }

    private void link(List<Integer> shaderModules) {
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            System.out.println("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));
        }

        shaderModules.forEach(s -> glDetachShader(programId, s));
        shaderModules.forEach(GL30::glDeleteShader);
    }

    public void unbind() {
        glUseProgram(0);
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
