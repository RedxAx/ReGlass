//#if MC >= 26.3
#version 450
//#else
#version 150
//#endif
//#if MC >= 26.3
layout(location = 0) in vec3 Position;
//#else
in vec3 Position;
//#endif
//#if MC >= 26.3
layout(location = 0) out vec2 texCoord;
//#else
out vec2 texCoord;
//#endif
void main() {
    texCoord = Position.xy;
    vec2 ndc = Position.xy * 2.0 - 1.0;
    gl_Position = vec4(ndc, 0.0, 1.0);
}