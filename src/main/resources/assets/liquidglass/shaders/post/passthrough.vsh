#version 150

in vec3 Position;

out vec2 texCoord;

void main() {
    gl_Position = vec4(Position.x * 2.0 - 1.0, Position.y * -2.0 + 1.0, 0.0, 1.0);
    texCoord = Position.xy;
}