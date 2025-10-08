#version 150
uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D Sampler2;
uniform sampler2D Sampler3;
uniform sampler2D Sampler4;
uniform sampler2D Sampler5;
layout(std140) uniform SamplerInfo { vec2 OutSize; vec2 InSize; };
layout(std140) uniform CustomUniforms {
    float Time;
    vec4 Mouse;
    float ScreenWantsBlur;
    vec3 RIM_LIGHT_VEC;
    vec4 RIM_LIGHT_COLOR;
    float EPS_PIX;
    float DebugStep;
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
    vec4 Shadow0[MAX_WIDGETS];
    vec4 ShadowColor[MAX_WIDGETS];
    vec4 Extra0[MAX_WIDGETS];
};
layout(std140) uniform BgConfig { float ShadowExpand; float ShadowFactor; vec2 ShadowOffset; };
out vec4 fragColor;
#define PI 3.141592653589793

struct SDFResult { float dist; vec2 normal; float aspect; int index; };

vec2 screenToUV(vec2 screen, vec2 res) { return (screen.xy - 0.5 * res.xy) / res.y; }

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

SDFResult opHardUnion(SDFResult a, SDFResult b) { return (a.dist < b.dist) ? a : b; }

SDFResult opHardSubtract(SDFResult a, SDFResult b) {
    float d = max(a.dist, -b.dist);
    if (d == a.dist) return a;
    return SDFResult(d, -b.normal, a.aspect, a.index);
}

vec4 sampleBlur(int idx, vec2 uv) {
    if (idx <= 0) return texture(Sampler1, uv);
    if (idx == 1) return texture(Sampler2, uv);
    if (idx == 2) return texture(Sampler3, uv);
    if (idx == 3) return texture(Sampler4, uv);
    return texture(Sampler5, uv);
}

SDFResult fieldWidgets(vec2 p, vec2 inSize) {
    int n = int(Count + 0.5);
    if (n == 0) return SDFResult(1e6, vec2(0.0), 1.0, -1);
    SDFResult pos = SDFResult(1e6, vec2(0.0), 1.0, -1);
    bool hasPos = false;
    vec2 fragCoord = gl_FragCoord.xy;

    for (int i = 0; i < MAX_WIDGETS; i++) {
        if (i >= n) break;
        if (Smoothings[i].x < 0.0) continue;
        vec4 sc = ScissorRects[i];
        if (fragCoord.x < sc.x || fragCoord.y < sc.y || fragCoord.x > sc.z || fragCoord.y > sc.w) continue;
        vec4 rc = Rects[i];
        vec4 rr = Rads[i];
        vec2 cPx = vec2(rc.x + 0.5 * rc.z, rc.y + 0.5 * rc.w);
        vec2 c = screenToUV(cPx, inSize);
        vec2 b = 0.5 * vec2(rc.z, rc.w) / inSize.y;
        vec4 rad = rr / inSize.y;
        vec3 g = sdgBox(p - c, b, rad);
        float aspect = min(rc.z, rc.w) / max(rc.z, rc.w);
        SDFResult s = SDFResult(g.x, g.yz, aspect, i);
        if (!hasPos) { pos = s; hasPos = true; } else { pos = opSmoothUnion(pos, s, Smoothings[i].x); }
    }

    SDFResult f = pos;

    for (int i = 0; i < MAX_WIDGETS; i++) {
        if (i >= n) break;
        if (Smoothings[i].x >= 0.0) continue;
        vec4 sc = ScissorRects[i];
        if (fragCoord.x < sc.x || fragCoord.y < sc.y || fragCoord.x > sc.z || fragCoord.y > sc.w) continue;
        vec4 rc = Rects[i];
        vec4 rr = Rads[i];
        vec2 cPx = vec2(rc.x + 0.5 * rc.z, rc.y + 0.5 * rc.w);
        vec2 c = screenToUV(cPx, inSize);
        vec2 b = 0.5 * vec2(rc.z, rc.w) / inSize.y;
        vec4 rad = rr / inSize.y;
        vec3 g = sdgBox(p - c, b, rad);
        float aspect = min(rc.z, rc.w) / max(rc.z, rc.w);
        SDFResult s = SDFResult(g.x, g.yz, aspect, i);
        float repulsion = -Smoothings[i].x;
        SDFResult se = SDFResult(s.dist - repulsion, s.normal, s.aspect, s.index);
        f = opHardSubtract(f, se);
        f = opHardUnion(f, s);
    }

    return f;
}

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0/3.0, 1.0/3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}
float vec2ToAngle(vec2 v) { float a = atan(v.y, v.x); if (a < 0.0) a += 2.0 * PI; return a; }
vec3 vec2ToRgb(vec2 v) { float hue = vec2ToAngle(normalize(v)) / (2.0 * PI); return hsv2rgb(vec3(hue, 1.0, 1.0)); }

void main() {
    vec2 inSize = InSize; if (inSize.x <= 0.0 || inSize.y <= 0.0) inSize = vec2(textureSize(Sampler0, 0));
    vec2 UV = gl_FragCoord.xy / inSize;
    vec3 base = texture(Sampler0, UV).rgb;

    vec3 baseShadowed = base;
    int n = int(Count + 0.5);
    for (int i = 0; i < MAX_WIDGETS; i++) {
        if (i >= n) break;
        vec4 sc = ScissorRects[i];
        if (gl_FragCoord.x < sc.x || gl_FragCoord.y < sc.y || gl_FragCoord.x > sc.z || gl_FragCoord.y > sc.w) continue;
        vec4 rc = Rects[i];
        vec4 rr = Rads[i];
        vec2 cPx = vec2(rc.x + 0.5 * rc.z, rc.y + 0.5 * rc.w);
        vec2 pShadow = screenToUV(gl_FragCoord.xy + Shadow0[i].zw, inSize);
        vec2 c = screenToUV(cPx, inSize);
        vec2 b = 0.5 * vec2(rc.z, rc.w) / inSize.y;
        vec4 rad = rr / inSize.y;
        vec3 g = sdgBox(pShadow - c, b, rad);
        float expand = max(Shadow0[i].x, 1e-4);
        float factor = Shadow0[i].y;
        float sh = exp(-abs(g.x) * inSize.y / expand) * 0.6 * factor;
        vec3 scol = ShadowColor[i].rgb;
        float sa = clamp(ShadowColor[i].a * sh, 0.0, 1.0);
        baseShadowed = mix(baseShadowed, scol, sa);
    }

    vec2 p = screenToUV(gl_FragCoord.xy, inSize);
    SDFResult f = fieldWidgets(p, inSize);
    float merged = f.dist;

    int STEP = int(DebugStep + 0.5);

    if (STEP <= 0) {
        float px = 2.0 / inSize.y;
        vec3 col = (merged > 0.0 ? vec3(1.0) * merged : vec3(1.0) * (-merged) * 2.0) * 3.0;
        col = mix(col, vec3(1.0), 1.0 - smoothstep(0.5 / inSize.y - px, 0.5 / inSize.y + px, abs(merged)));
        fragColor = vec4(col, 1.0); return;
    }
    if (STEP <= 1) {
        float px = 2.0 / inSize.y;
        vec3 col = (merged > 0.0) ? vec3(0.9, 0.6, 0.3) : vec3(0.65, 0.85, 1.0);
        col *= 1.0 - exp(-0.03 * abs(merged) * inSize.y);
        col *= 0.6 + 0.4 * smoothstep(-0.5, 0.5, cos(0.25 * abs(merged) * inSize.y * 2.0));
        col = mix(col, vec3(1.0), 1.0 - smoothstep(1.5 / inSize.y - px, 1.5 / inSize.y + px, abs(merged)));
        fragColor = vec4(col, 1.0); return;
    }
    if (merged >= 0.0) { fragColor = vec4(baseShadowed, 1.0); return; }

    int idx = f.index;
    vec4 o0 = Optics0[idx];
    vec4 o1 = Optics1[idx];
    vec4 o2 = Optics2[idx];
    vec4 tint = Tints[idx];

    float refThickness = o0.x, refFactor = o0.y, refDisp = o0.z, refFresRange = o0.w;
    float refFresHard = o1.x / 100.0, refFresFac = o1.y / 100.0, glareRange = o1.z, glareHard = o1.w / 100.0;
    float glareConv = o2.x / 100.0, glareOpp = o2.y / 100.0, glareFac = o2.z / 100.0, glareAngle = o2.w;

    vec2 normal = f.normal; float nlen = length(normal); if (nlen > 1e-6) normal /= nlen;

    if (STEP <= 2) { vec3 normalColor = vec2ToRgb(normal); fragColor = vec4(normalColor, nlen); return; }

    float nmerged = -merged * inSize.y;
    float xR = 1.0 - nmerged / max(refThickness, 1e-6);
    float thetaI = asin(pow(xR, 2.0));
    float thetaT = asin((1.0 / max(refFactor, 1e-6)) * sin(thetaI));
    float edgeFactor = -tan(thetaT - thetaI);
    if (nmerged >= refThickness) edgeFactor = 0.0;

    if (STEP <= 3) { fragColor = vec4(vec3(edgeFactor), 1.0); return; }

    if (STEP <= 4) {
        vec3 normalColor = vec2ToRgb(normal);
        float ring = max(edgeFactor, 0.0);
        vec3 outC = clamp(normalColor * ring * 4.0, 0.0, 1.0);
        fragColor = vec4(outC, 1.0); return;
    }

    int blurIndex = int(Extra0[idx].x + 0.5);
    vec2 refrOffset = -normal * edgeFactor * 0.08 * vec2(inSize.y / inSize.x, 1.0);

    if (STEP <= 5) { vec4 outColor = sampleBlur(blurIndex, UV); fragColor = outColor; return; }
    if (STEP <= 6) { vec4 outColor = sampleBlur(blurIndex, UV + refrOffset); fragColor = outColor; return; }

    float fresnelFactor = clamp(pow(1.0 + merged * inSize.y / 1500.0 * pow(500.0 / max(refFresRange, 1e-6), 2.0) + refFresHard, 5.0), 0.0, 1.0);

    if (STEP <= 7) {
        vec4 blurredPixel = sampleBlur(blurIndex, UV + refrOffset);
        vec4 outColor = mix(blurredPixel, vec4(1.0), (fresnelFactor * refFresFac * 0.7) * nlen);
        fragColor = outColor; return;
    }

    if (STEP <= 8) {
        vec4 blurredPixel = sampleBlur(blurIndex, UV + refrOffset);
        float glareGeo = clamp(pow(1.0 + merged * inSize.y / 1500.0 * pow(500.0 / max(glareRange, 1e-6), 2.0) + glareHard, 5.0), 0.0, 1.0);
        float ang = (atan(normal.y, normal.x) - PI * 0.25 + glareAngle) * 2.0;
        int farSide = 0; if ((ang > PI * (2.0 - 0.5) && ang < PI * (4.0 - 0.5)) || (ang < PI * (0.0 - 0.5))) farSide = 1;
        float gAngle = (0.5 + sin(ang) * 0.5) * (farSide == 1 ? 1.2 * glareOpp : 1.2) * glareFac;
        gAngle = clamp(pow(gAngle, 0.1 + glareConv * 2.0), 0.0, 1.0);
        vec4 outColor = mix(blurredPixel, vec4(1.0), gAngle * glareGeo);
        fragColor = outColor; return;
    }

    vec4 blurredPixel = sampleBlur(blurIndex, UV + refrOffset);
    vec4 dispPixel;
    { const float NR = 0.98, NG = 1.00, NB = 1.02;
        dispPixel.r = sampleBlur(blurIndex, UV + refrOffset * (1.0 - (NR - 1.0) * refDisp)).r;
        dispPixel.g = sampleBlur(blurIndex, UV + refrOffset * (1.0 - (NG - 1.0) * refDisp)).g;
        dispPixel.b = sampleBlur(blurIndex, UV + refrOffset * (1.0 - (NB - 1.0) * refDisp)).b;
        dispPixel.a = 1.0; }

    vec4 outColor = mix(dispPixel, vec4(tint.rgb, 1.0), tint.a * 0.8);

    float fres = fresnelFactor;
    vec3 fresTint = mix(vec3(1.0), tint.rgb, tint.a * 0.5);
    outColor = mix(outColor, vec4(fresTint, 1.0), fres * refFresFac * 0.7 * nlen);

    float glareGeo = clamp(pow(1.0 + merged * inSize.y / 1500.0 * pow(500.0 / max(glareRange, 1e-6), 2.0) + glareHard, 5.0), 0.0, 1.0);
    float ang = (atan(normal.y, normal.x) - PI * 0.25 + glareAngle) * 2.0;
    int farSide = 0; if ((ang > PI * (2.0 - 0.5) && ang < PI * (4.0 - 0.5)) || (ang < PI * (0.0 - 0.5))) farSide = 1;
    float gAngle = (0.5 + sin(ang) * 0.5) * (farSide == 1 ? 1.2 * glareOpp : 1.2) * glareFac;
    gAngle = clamp(pow(gAngle, 0.1 + glareConv * 2.0), 0.0, 1.0);
    vec3 glareMix = mix(blurredPixel.rgb, tint.rgb, tint.a * 0.5);
    vec3 glareCol = glareMix;
    outColor = mix(outColor, vec4(glareCol, 1.0), gAngle * glareGeo * nlen);

    vec3 finalCol = mix(outColor.rgb, baseShadowed, smoothstep(-0.001, 0.001, merged));
    fragColor = vec4(finalCol, 1.0);
}