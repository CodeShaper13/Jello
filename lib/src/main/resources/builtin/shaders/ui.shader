{
  "shaders": [
    {
      "type": "vertex",
      "source": "
      	#version 330

		layout (location=0) in vec3 inPosition;
		layout (location=1) in vec2 texCoord;
		
		out vec2 outTextCoord;

		uniform mat4 modelMatrix;
		uniform mat4 _uiMatrix;
		uniform vec3 _size;

		void main() {
	    	vec3 inPos = vec3(inPosition.x, inPosition.y, inPosition.z);
	    	inPos *= _size;

	    	gl_Position = _uiMatrix * modelMatrix * vec4(inPos, 1);
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
		
		uniform sampler2D txtSampler;
		uniform vec4 _uiColor;
				
		void main() {
	    	fragColor = texture(txtSampler, outTextCoord) * _uiColor;
		}
      "
    }
  ]
}