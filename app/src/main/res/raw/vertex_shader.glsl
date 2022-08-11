#version 300 es

precision mediump float;

in vec3 a_Position;
uniform mat4 u_viewProjectionMatrix;

out vec3 v_Position;

void main() {
    v_Position = a_Position;
    gl_Position = u_viewProjectionMatrix * vec4(a_Position, 1.0);
}