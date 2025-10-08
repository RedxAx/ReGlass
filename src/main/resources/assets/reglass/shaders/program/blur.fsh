#version 150

uniform sampler2D DiffuseSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform Config {
    vec4 Params;
    float Weights[65];
};

in vec2 texCoord;
out vec4 fragColor;

void main() {
    int radius = int(Params.z + 0.5);
    radius = clamp(radius, 0, 64);

    vec2 delta = Params.xy / OutSize;

    vec4 sum = texture(DiffuseSampler, texCoord) * Weights[0];

    for (int i = 1; i <= radius; ++i) {
        float w = Weights[i];
        vec2 offs = delta * float(i);
        sum += texture(DiffuseSampler, texCoord + offs) * w;
        sum += texture(DiffuseSampler, texCoord - offs) * w;
    }

    fragColor = sum;
}