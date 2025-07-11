{
	"enable_depth_mask": false,
	"shaders": [
		{
			"type": "vertex",
			"source": "
				#version 330
					
				layout (location=0) in vec3 inPosition;
				layout (location=1) in vec2 texCoord;
				
				out vec2 outTextCoord;
				
				uniform mat4 projectionMatrix;
				uniform mat4 viewMatrix;
				uniform mat4 modelMatrix;
				
				void main() {
				    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(inPosition, 1.0);
				    outTextCoord = texCoord;
				}
		  	"
	    },
	    {
	      	"type": "fragment",
	     	"source": "		
				#version 330
				
				in vec2 outTextCoord;
				
				out vec4 fragColor;
				
				uniform sampler2D mainTexture;
				uniform vec4 mainColor;
				
				void main() {
				    fragColor = mainColor * texture(mainTexture, outTextCoord);
				}
			"
		}
	]
}