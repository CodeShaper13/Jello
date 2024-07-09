#version 330

//in vec3 outColor;
in vec2 outTextCoord;

out vec4 fragColor;

uniform sampler2D txtSampler;

void main()
{
    //fragColor = vec4(outColor, 1.0);
    fragColor = texture(txtSampler, outTextCoord);
}