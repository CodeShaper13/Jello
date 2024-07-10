#version 330

layout(location = 0) in vec3 inPosition;
out vec3 nearPoint;
out vec3 farPoint;

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

vec3 unproject_point(float x, float y, float z) {
    mat4 inv = inverse(projectionMatrix * viewMatrix);
    vec4 unproj_point = inv * vec4(x, y, z, 1.f);
    return unproj_point.xyz / unproj_point.w;
}

void main() {
    vec2 p = inPosition.xy;
    nearPoint = unproject_point(p.x, p.y, -1.f);
    farPoint  = unproject_point(p.x, p.y,  1.f);
    gl_Position = vec4(inPosition.xy, 0.0f, 1.0f);
}
