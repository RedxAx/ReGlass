#version 150

uniform sampler2D iChannel0Sampler;
uniform sampler2D iChannel1Sampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

in vec2 texCoord;
out vec4 fragColor;

vec3 blendScreen(vec3 a, vec3 b) {
    return 1.0 - (1.0 - a) * (1.0 - b);
}

void main() {
    float threshold = 0.2;
    float intensity = 1.0;

    vec2 uv = texCoord;

    vec4 blurred = texture(iChannel1Sampler, uv);
    vec4 highlight = clamp(blurred - threshold, 0.0, 1.0) * (1.0 / (1.0 - threshold)) * intensity;

    vec3 baseColor = texture(iChannel0Sampler, uv).rgb;

    fragColor = vec4(blendScreen(baseColor, highlight.rgb), 1.0);
}