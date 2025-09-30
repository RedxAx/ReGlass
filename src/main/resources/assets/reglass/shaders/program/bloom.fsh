#version 150

uniform sampler2D iChannel0Sampler;
uniform sampler2D iChannel1Sampler;
layout(std140) uniform SamplerInfo { vec2 OutSize; vec2 InSize; };

in vec2 texCoord;
out vec4 fragColor;

vec3 blendScreen(vec3 a, vec3 b) {
    return 1.0 - (1.0 - a) * (1.0 - b);
}

void main() {
    float threshold = 0.2;
    float intensity = 1.0;
    vec3 base = texture(iChannel0Sampler, texCoord).rgb;
    vec3 blurred = texture(iChannel1Sampler, texCoord).rgb;
    vec3 hi = clamp(blurred - threshold, 0.0, 1.0) * (1.0 / (1.0 - threshold)) * intensity;
    fragColor = vec4(blendScreen(base, hi), 1.0);

}