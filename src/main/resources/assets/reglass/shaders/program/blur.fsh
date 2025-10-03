#version 150

const int SAMPLES = 16;
const float SIGMA = float(SAMPLES) * 0.25;

uniform sampler2D DiffuseSampler;
layout(std140) uniform SamplerInfo { vec2 OutSize; vec2 InSize; };
layout(std140) uniform Config { vec2 BlurDir; };

in vec2 texCoord;
out vec4 fragColor;

float gWeight(int i) {
    float d = float(i);
    return exp(-0.5 * (d * d) / (SIGMA * SIGMA));
}

void main() {
    vec2 delta = BlurDir / OutSize;
    vec4 sum = vec4(0.0);
    float acc = 0.0;
    int m = SAMPLES / 2;
    for (int i = -m; i <= m; ++i) {
        float w = gWeight(i);
        sum += texture(DiffuseSampler, texCoord + delta * float(i)) * w;
        acc += w;
    }
    fragColor = sum / acc;

}