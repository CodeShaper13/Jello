{
  "shaders": [
    {
      "type": "vertex",
      "source":

"
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
"
    },
    {
      "type": "fragment",
      "source":
"
// pieced together from
// https://github.com/mnerv/lumagl/blob/trunk/src/grid.cpp
// and
// https://asliceofrendering.com/scene%20helper/2020/01/05/InfiniteGrid/

#version 330

uniform float near;
uniform float far;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
in vec3 nearPoint;
in vec3 farPoint;
in mat4 fragView;
in mat4 fragProj;
layout(location = 0) out vec4 outColor;

vec4 grid(vec3 fragPos3D, float scale, bool drawAxis) {
    vec2 coord = fragPos3D.xz * scale;
    vec2 derivative = fwidth(coord);
    vec2 grid = abs(fract(coord - 0.5) - 0.5) / derivative;
    float line = min(grid.x, grid.y);
    float minimumz = min(derivative.y, 1);
    float minimumx = min(derivative.x, 1);
    vec4 color = vec4(0.2, 0.2, 0.2, 1.0 - min(line, 1.0));

    // z axis
    if(fragPos3D.x > -1 * minimumx && fragPos3D.x < 1 * minimumx) {
        color.z = 1.0;
    }

    // x axis
    if(fragPos3D.z > -1 * minimumz && fragPos3D.z < 1 * minimumz) {
        color.x = 1.0;
    }

    return color;
}

float computeDepth(vec3 point) {
    vec4 clip_space = projectionMatrix * viewMatrix * vec4(point, 1.0);
    float clip_space_depth = clip_space.z / clip_space.w;
    float far  = gl_DepthRange.far;
    float near = gl_DepthRange.near;
    float depth = (((far - near) * clip_space_depth) + near + far) / 2.0;
    return depth;
}

float computeLinearDepth(vec3 pos) {
    vec4 clip_space_pos = projectionMatrix * viewMatrix * vec4(pos.xyz, 1.0);
    float clip_space_depth = (clip_space_pos.z / clip_space_pos.w) * 2.0 - 1.0; // put back between -1 and 1
    float linearDepth = (2.0 * near * far) / (far + near - clip_space_depth * (far - near)); // get linear value between 0.01 and 100
    return linearDepth / far; // normalize
}

void main() {
    float t = -nearPoint.y / (farPoint.y - nearPoint.y);
    vec3 fragPos3D = nearPoint + t * (farPoint - nearPoint);

    gl_FragDepth = computeDepth(fragPos3D);

    outColor = (
        grid(fragPos3D, 0.1, true) +
        grid(fragPos3D, 1, true)) * float(t > 0);

    float fade = smoothstep(0.04, 0.0, computeLinearDepth(fragPos3D));
    outColor.a *= fade;
}
"
    }
  ]
}