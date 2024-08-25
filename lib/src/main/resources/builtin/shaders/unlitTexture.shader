{
  "shaders": [
    {
      "type": "vertex",
      "source": "
		#version 330
		
		layout (location=0) in vec3 inPosition;
		layout (location=1) in vec2 texCoord;
		
		out vec3 outPosition;
		out vec2 outTextCoord;
		
		uniform mat4 projectionMatrix;
		uniform mat4 viewMatrix;
		uniform mat4 modelMatrix;
		
		void main() {
			mat4 modelViewMatrix = viewMatrix * modelMatrix;
		    vec4 mvPosition =  modelViewMatrix * vec4(inPosition, 1.0);
		    gl_Position = projectionMatrix * mvPosition;
		    outPosition = mvPosition.xyz;
		    outTextCoord = texCoord;
		}
	  "
    },
    {
      "type": "fragment",
      "source": "		
		#version 330
		
		in vec3 outPosition;
		in vec2 outTextCoord;
		
		out vec4 fragColor;
		
		struct Fog {
		    vec3 color;
		    float density;
		};
				
		uniform sampler2D mainTexture;
		uniform vec4 mainColor;
		uniform Fog _fog;
		
		vec4 applyFog(vec3 pos, vec4 color) {
   			float distance = length(pos);
    		float fogFactor = 1.0 / exp((distance * _fog.density) * (distance * _fog.density));
    		fogFactor = clamp(fogFactor, 0.0, 1.0);

    		vec3 resultColor = mix(_fog.color, color.xyz, fogFactor);
    		return vec4(resultColor.xyz, color.w);
		}
		
		void main() {
		    fragColor = mainColor * texture(mainTexture, outTextCoord);
		    
		    if(fragColor.a < 0.1) {
        		discard;
    		} else {		    
		        if (_fog.density > 0) {
			        fragColor = applyFog(outPosition, fragColor);
			    }
		    }
		}
	  "
    }
  ]
}