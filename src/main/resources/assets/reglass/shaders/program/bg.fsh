#version 150

uniform sampler2D Sampler0;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform BgConfig {
    float ShadowExpand;
    float ShadowFactor;
    vec2 ShadowOffset;
};

#define MAX_WIDGETS 64
layout(std140) uniform WidgetInfo {
    float Count;
    vec4 Rects[MAX_WIDGETS];
    vec4 Rads[MAX_WIDGETS];
    vec4 Tints[MAX_WIDGETS];
    vec4 Optics0[MAX_WIDGETS];
    vec4 Optics1[MAX_WIDGETS];
    vec4 Optics2[MAX_WIDGETS];
    vec4 Smoothings[MAX_WIDGETS];
    vec4 ScissorRects[MAX_WIDGETS];
};

out vec4 fragColor;

#define PI 3.141592653589793

struct SDFResult {
    float dist;
    vec2 normal;
    float aspect;
    int index;
};

float lerp01(float a, float b, float x) {
    return clamp((x - a) / (b - a), 0.0, 1.0);
}

vec2 screenToUV(vec2 screen, vec2 res) {
    return (screen.xy - 0.5 * res.xy) / res.y;
}

vec3 sdgBox(in vec2 p, in vec2 b, vec4 ra) {
    ra.xy = (p.x > 0.0) ? ra.xy : ra.zw;
    float r = (p.y > 0.0) ? ra.x : ra.y;
    vec2 w = abs(p) - (b - r);
    vec2 s = vec2(p.x < 0.0 ? -1 : 1, p.y < 0.0 ? -1 : 1);
    float g = max(w.x, w.y);
    vec2 q = max(w, 0.0);
    float l = length(q);
    float dist = (g > 0.0) ? l - r : g - r;
    vec2 n = (g > 0.0) ? (q / max(l, 1e-6)) : ((w.x > w.y) ? vec2(1, 0) : vec2(0, 1));
    return vec3(dist, s * n);
}

SDFResult opSmoothUnion(in SDFResult a, in SDFResult b, in float k) {
    if (k == 0.0) return (a.dist < b.dist) ? a : b;
    float h = clamp(0.5 + 0.5 * (a.dist - b.dist) / k, 0.0, 1.0);
    float d = mix(a.dist, b.dist, h) - k * h * (1.0 - h);
    vec2 n = normalize(mix(a.normal, b.normal, h));
    float aspect = mix(a.aspect, b.aspect, h);
    int index = (a.dist < b.dist) ? a.index : b.index;
    return SDFResult(d, n, aspect, index);
}

SDFResult opHardUnion(SDFResult a, SDFResult b) {
    return (a.dist < b.dist) ? a : b;
}

SDFResult opHardSubtract(SDFResult a, SDFResult b) {
    float d = max(a.dist, -b.dist);
    if (d == a.dist) return a;
    return SDFResult(d, -b.normal, a.aspect, a.index);
}

SDFResult fieldWidgets(vec2 p, vec2 inSize) {
    int n = int(Count + 0.5);
    if (n == 0) return SDFResult(1e6, vec2(0.0), 1.0, -1);

    SDFResult posShapes = SDFResult(1e6, vec2(0.0), 1.0, -1);
    bool hasPos = false;
    vec2 fragCoord = gl_FragCoord.xy + ShadowOffset;

    for (int i = 0; i < MAX_WIDGETS; i++) {
        if (i >= n) break;
        if (Smoothings[i].x < 0.0) continue;

        vec4 sc = ScissorRects[i];
        if (fragCoord.x < sc.x || fragCoord.y < sc.y || fragCoord.x > sc.z || fragCoord.y > sc.w)
        continue;

        vec4 rc = Rects[i];
        vec4 rr = Rads[i];
        vec2 cPx = vec2(rc.x + 0.5 * rc.z, rc.y + 0.5 * rc.w);
        vec2 c = screenToUV(cPx, inSize);
        vec2 b = 0.5 * vec2(rc.z, rc.w) / inSize.y;
        vec4 rad = rr / inSize.y;
        vec3 gvec = sdgBox(p - c, b, rad);
        float aspect = min(rc.z, rc.w) / max(rc.z, rc.w);
        SDFResult g = SDFResult(gvec.x, gvec.yz, aspect, i);

        if (!hasPos) {
            posShapes = g; hasPos = true;
        } else {
            posShapes = opSmoothUnion(posShapes, g, Smoothings[i].x);
        }
    }

    SDFResult f = posShapes;

    for (int i = 0; i < MAX_WIDGETS; i++) {
        if (i >= n) break;
        if (Smoothings[i].x >= 0.0) continue;

        vec4 sc = ScissorRects[i];
        if (fragCoord.x < sc.x || fragCoord.y < sc.y || fragCoord.x > sc.z || fragCoord.y > sc.w)
        continue;

        vec4 rc = Rects[i];
        vec4 rr = Rads[i];
        vec2 cPx = vec2(rc.x + 0.5 * rc.z, rc.y + 0.5 * rc.w);
        vec2 c = screenToUV(cPx, inSize);
        vec2 b = 0.5 * vec2(rc.z, rc.w) / inSize.y;
        vec4 rad = rr / inSize.y;
        vec3 gvec = sdgBox(p - c, b, rad);
        float aspect = min(rc.z, rc.w) / max(rc.z, rc.w);
        SDFResult g = SDFResult(gvec.x, gvec.yz, aspect, i);

        float rep = -Smoothings[i].x;
        SDFResult ge = SDFResult(g.dist - rep, g.normal, g.aspect, g.index);

        f = opHardSubtract(f, ge);
        f = opHardUnion(f, g);
    }

    return f;
}

void main() {
    vec2 inSize = InSize;
    if (inSize.x <= 0.0 || inSize.y <= 0.0) {
        inSize = vec2(textureSize(Sampler0, 0));
    }

    vec2 UV = gl_FragCoord.xy / inSize;
    vec2 p = screenToUV(gl_FragCoord.xy + ShadowOffset, inSize);

    vec3 base = texture(Sampler0, UV).rgb;

    SDFResult f = fieldWidgets(p, inSize);

    float dist = f.dist;
    float shadow = exp(-abs(dist) * inSize.y / max(ShadowExpand, 1e-4)) * 0.6 * ShadowFactor;

    vec3 col = base - vec3(shadow);
    fragColor = vec4(col, 1.0);
}