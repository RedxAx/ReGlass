#version 150

in vec3 Position;
in vec2 UV0;

out vec2 texCoord;

void main() {
    // Map the [0,1] input quad vertices to the [-1,1] clip space, flipping Y.
    gl_Position = vec4(Position.x * 2.0 - 1.0, Position.y * -2.0 + 1.0, 0.0, 1.0);
    texCoord = UV0;
}