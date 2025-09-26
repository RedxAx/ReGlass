#version 150

uniform sampler2D MainSampler;

layout(std140) uniform Config {
    vec2 BlurDir;
};

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

void main() {
    vec2 delta = BlurDir * oneTexel;

    vec4 color = vec4(0.0);
    color += texture(MainSampler, texCoord - delta);
    color += texture(MainSampler, texCoord + delta);
    color += texture(MainSampler, texCoord - delta * 2.0);
    color += texture(MainSampler, texCoord + delta * 2.0);

    fragColor = color / 4.0;
}