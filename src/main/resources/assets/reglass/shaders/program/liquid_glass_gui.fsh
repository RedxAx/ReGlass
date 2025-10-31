#version 150
uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D Sampler2;
uniform sampler2D Sampler3;
uniform sampler2D Sampler4;
uniform sampler2D Sampler5;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform CustomUniforms {
    float Time;
    vec4 Mouse;
    float ScreenWantsBlur;
    vec3 RIM_LIGHT_VEC;
    vec4 RIM_LIGHT_COLOR;
    float EPS_PIX;
    float DebugStep;
    float Pixelated;
    float PixelGridSize;
    float HoverScalePx;
    float FocusScalePx;
    float FocusBorderWidthPx;
    float FocusBorderIntensity;
    float FocusBorderSpeed;
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

layout(std140) uniform BgConfig {
    float ShadowExpand;
    float ShadowFactor;
    vec2 ShadowOffset;
};

out vec4 fragColor;

struct SDFResult { float dist; vec2 normal; float aspect; int index; };

vec2 screenToUV(vec2 screen, vec2 res) {
    return (screen.xy - 0.5 * res.xy) / res.y;
}

vec3 sdgBox(in vec2 p, in vec2 b, vec4 ra) {
    ra.xy = (p.x > 0.0) ? ra.xy : ra.zw;
    float r = (p.y > 0.0) ? ra.x : ra.y;
    vec2 w = abs(p) - (b - r);
    vec2 s = vec2(p.x < 0.0 ? -1.0 : 1.0, p.y < 0.0 ? -1.0 : 1.0);
    float g = max(w.x, w.y);
    vec2 q = max(w, 0.0);
    float l = length(q);
    float dist = (g > 0.0) ? l - r : g - r;
    vec2 n = (g > 0.0) ? (q / max(l, 1e-6)) : ((w.x > w.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0));
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

SDFResult fieldWidgets(vec2 p, vec2 inSize, vec2 fragCoord) {
    int n = int(Count + 0.5);
    if (n == 0) return SDFResult(1e6, vec2(0.0), 1.0, -1);

    SDFResult pos = SDFResult(1e6, vec2(0.0), 1.0, -1);
    bool hasPos = false;

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

        vec4 extra = Extra0[i];
        float scaleOff = (HoverScalePx * extra.y + FocusScalePx * extra.z) / inSize.y;
        float dist = g.x - scaleOff;

        float aspect = min(rc.z, rc.w) / max(rc.z, rc.w);
        SDFResult s = SDFResult(dist, g.yz, aspect, i);

        if (!hasPos) { pos = s; hasPos = true; }
        else { pos = opSmoothUnion(pos, s, Smoothings[i].x); }
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

        vec4 extra = Extra0[i];
        float scaleOff = (HoverScalePx * extra.y + FocusScalePx * extra.z) / inSize.y;
        float dist = g.x - scaleOff;

        float aspect = min(rc.z, rc.w) / max(rc.z, rc.w);
        SDFResult s = SDFResult(dist, g.yz, aspect, i);

        float repulsion = -Smoothings[i].x;
        SDFResult se = SDFResult(s.dist - repulsion, s.normal, s.aspect, s.index);
        f = opHardSubtract(f, se);
        f = opHardUnion(f, s);
    }

    return f;
}

void main() {
    vec2 inSize = InSize;
    if (inSize.x <= 0.0 || inSize.y <= 0.0) inSize = vec2(textureSize(Sampler0, 0));

    vec2 coord = gl_FragCoord.xy;
    vec2 uv = coord / inSize;
    vec3 base = texture(Sampler0, uv).rgb;

    vec3 baseShadowed = base;
    int n = int(Count + 0.5);
    for (int i = 0; i < MAX_WIDGETS; i++) {
        if (i >= n) break;

        vec4 sc = ScissorRects[i];
        if (coord.x < sc.x || coord.y < sc.y || coord.x > sc.z || coord.y > sc.w) continue;

        vec4 rc = Rects[i];
        vec4 rr = Rads[i];
        vec2 cPx = vec2(rc.x + 0.5 * rc.z, rc.y + 0.5 * rc.w);

        vec2 pShadow = screenToUV(coord + Shadow0[i].zw, inSize);
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

    vec2 p = screenToUV(coord, inSize);
    SDFResult f = fieldWidgets(p, inSize, coord);
    float merged = f.dist;

    int STEP = int(DebugStep + 0.5);
    if (STEP <= 1) {
        vec3 col = (merged > 0.0) ? vec3(0.9, 0.6, 0.3) : vec3(0.65, 0.85, 1.0);
        col *= 1.0 - exp(-0.03 * abs(merged) * inSize.y);
        fragColor = vec4(col, 1.0);
        return;
    }

    if (merged >= 0.0) {
        fragColor = vec4(baseShadowed, 1.0);
        return;
    }

    vec2 coordPix = coord;
    if (Pixelated > 0.5) {
        coordPix = floor(coord / PixelGridSize) * PixelGridSize + 0.5 * PixelGridSize;
    }
    vec2 uvPix = coordPix / inSize;

    int idx = f.index;
    vec4 o0 = Optics0[idx];
    vec4 o1 = Optics1[idx];
    vec4 o2 = Optics2[idx];
    vec4 tint = Tints[idx];
    vec4 extra = Extra0[idx];

    float refThickness = o0.x;
    float refFactor = o0.y;
    float refDisp = o0.z;
    float refFresRange = o0.w;

    float refFresHard = o1.x / 100.0;
    float refFresFac = o1.y / 100.0;
    float glareRange = o1.z;
    float glareHard = o1.w / 100.0;

    float hoverK = extra.y;
    float focusK = extra.z;
    int blurIndex = int(extra.x + 0.5);

    vec2 normal = f.normal;
    float nlen = length(normal);
    if (nlen > 1e-6) normal /= nlen;

    float nmerged = -merged * inSize.y;
    float xR = 1.0 - nmerged / max(refThickness, 1e-6);
    float thetaI = asin(pow(xR, 2.0));
    float thetaT = asin((1.0 / max(refFactor, 1e-6)) * sin(thetaI));
    float edgeFactor = -tan(thetaT - thetaI);
    if (nmerged >= refThickness) edgeFactor = 0.0;

    vec2 refrOffset = -normal * edgeFactor * 0.08 * vec2(inSize.y / inSize.x, 1.0);
    vec4 blurred = sampleBlur(blurIndex, uvPix + refrOffset);

    vec4 dispPixel;
    {
        const float NR = 0.985;
        const float NG = 1.000;
        const float NB = 1.015;
        dispPixel.r = sampleBlur(blurIndex, uvPix + refrOffset * (1.0 - (NR - 1.0) * refDisp)).r;
        dispPixel.g = sampleBlur(blurIndex, uvPix + refrOffset * (1.0 - (NG - 1.0) * refDisp)).g;
        dispPixel.b = sampleBlur(blurIndex, uvPix + refrOffset * (1.0 - (NB - 1.0) * refDisp)).b;
        dispPixel.a = 1.0;
    }

    vec4 outColor = mix(dispPixel, vec4(tint.rgb, 1.0), tint.a * 0.8);

    float fresnelFactor = clamp(pow(1.0 + merged * inSize.y / 1500.0 * pow(500.0 / max(refFresRange, 1e-6), 2.0) + refFresHard, 5.0), 0.0, 1.0);
    vec3 fresTint = mix(vec3(1.0), tint.rgb, tint.a * 0.5);
    outColor = mix(outColor, vec4(fresTint, 1.0), fresnelFactor * refFresFac * 0.7 * nlen);

    float glareGeo = clamp(pow(1.0 + merged * inSize.y / 1500.0 * pow(500.0 / max(glareRange, 1e-6), 2.0) + glareHard, 5.0), 0.0, 1.0);
    vec3 glareMix = mix(blurred.rgb, tint.rgb, tint.a * 0.5);
    outColor.rgb = mix(outColor.rgb, glareMix, 0.25 * glareGeo * nlen);

    float edgeProx = exp(-abs(merged) * inSize.y / 9.0);

    if (hoverK > 0.0001) {
        vec2 tdir = normalize(vec2(-normal.y, normal.x));
        float refBoost = 1.0 + 0.35 * hoverK * edgeProx;
        vec2 refrHover = -normal * edgeFactor * 0.08 * refBoost * vec2(inSize.y / inSize.x, 1.0);
        vec2 chromaT = tdir * 0.0008 * hoverK * edgeProx * vec2(inSize.y / inSize.x, 1.0);
        vec3 subtle = vec3(
        sampleBlur(blurIndex, uvPix + refrHover + chromaT).r,
        sampleBlur(blurIndex, uvPix + refrHover).g,
        sampleBlur(blurIndex, uvPix + refrHover - chromaT).b
        );
        vec3 add = mix(subtle, vec3(1.0), 0.08) * (0.15 + 0.35 * edgeProx) * hoverK;
        outColor.rgb = clamp(outColor.rgb + add, 0.0, 1.0);
    }

    if (focusK > 0.0001) {
        vec4 rc = Rects[idx];
        vec2 centerPx = vec2(rc.x + 0.5 * rc.z, rc.y + 0.5 * rc.w);
        vec2 v = (coord - centerPx) / inSize.y;

        float phi = atan(v.y, v.x);
        float phi0 = Time * FocusBorderSpeed;
        float c = 0.5 + 0.5 * cos(phi - phi0);
        float sweep = smoothstep(0.75, 1.0, c);

        float mergedPx = merged * inSize.y;
        float w = max(FocusBorderWidthPx, 1e-3);
        float outside = step(0.0, mergedPx);
        float band = exp(-pow(max(0.0, mergedPx) / w, 2.0)) * outside;

        float intensity = FocusBorderIntensity * focusK;
        vec3 ringTint = mix(vec3(1.0), tint.rgb, 0.2);
        vec3 ring = ringTint * (band * sweep) * intensity;
        outColor.rgb = clamp(outColor.rgb + ring, 0.0, 1.0);
    }

    vec3 finalCol = mix(outColor.rgb, baseShadowed, smoothstep(-0.001, 0.001, merged));
    fragColor = vec4(finalCol, 1.0);
}