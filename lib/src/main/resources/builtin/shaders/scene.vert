#version 330

layout (location=0) in vec3 inPosition;
//layout (location=1) in vec3 color;
layout (location=1) in vec2 texCoord;

//out vec3 outColor;
out vec2 outTextCoord;

uniform mat4 projectionMatrix;
uniform mat4 modelMatrix;

void main()
{
    gl_Position = projectionMatrix * modelMatrix * vec4(inPosition, 1.0);
    //outColor = color;
    outTextCoord = texCoord;
}