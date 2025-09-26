#version 150

uniform sampler2D iChannel0;
uniform sampler2D iChannel1;
uniform vec2 ScreenSize;

in vec2 texCoord;
out vec4 fragColor;

vec3 blendScreen(vec3 a, vec3 b) {
    return 1. - (1. - a) * (1. - b);
}

vec4 simpleBlur(vec2 p, sampler2D src, float size, vec2 textureSize) {
    vec2 texelSize = size / textureSize;
    vec4 color = vec4(0.0);

    color += texture(src, p + vec2(texelSize.x, 0.0));
    color += texture(src, p + vec2(-texelSize.x, 0.0));
    color += texture(src, p + vec2(0.0, texelSize.y));
    color += texture(src, p + vec2(0.0, -texelSize.y));
    color += texture(src, p + vec2(texelSize.x, texelSize.y));
    color += texture(src, p + vec2(-texelSize.x, texelSize.y));
    color += texture(src, p + vec2(texelSize.x, -texelSize.y));
    color += texture(src, p + vec2(-texelSize.x, -texelSize.y));
    color += texture(src, p);

    return color / 9.0;
}

void main()
{
    float threshold = 0.2;
    float intensity = 1.0;
    float blurSize = 4.0;

    vec2 uv = texCoord;

    vec4 blurred = simpleBlur(uv, iChannel1, blurSize, ScreenSize);
    vec4 highlight = clamp(blurred - threshold, 0.0, 1.0) * (1.0 / (1.0 - threshold)) * intensity;

    vec3 baseColor = texture(iChannel0, uv).rgb;

    fragColor = vec4(
    blendScreen(baseColor, highlight.rgb),
    1.0
    );
}