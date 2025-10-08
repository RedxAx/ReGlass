#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

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
};

layout(std140) uniform BgConfig {
    float ShadowExpand;
    float ShadowFactor;
    vec2 ShadowOffset;
};

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
float vec2ToAngle(vec2 v) {
    float a = atan(v.y, v.x);
    if (a < 0.0) a += 2.0 * PI;
    return a;
}
vec3 vec2ToRgb(vec2 v) {
    float hue = vec2ToAngle(v) / (2.0 * PI);
    return hsv2rgb(vec3(hue, 1.0, 1.0));
}

const vec3 D65_WHITE = vec3(0.95045592705, 1.0, 1.08905775076);
const vec3 D50_WHITE = vec3(0.96429567643, 1.0, 0.82510460251);
vec3 WHITE = D65_WHITE;
const mat3 RGB_TO_XYZ_M = mat3(
0.4124, 0.3576, 0.1805,
0.2126, 0.7152, 0.0722,
0.0193, 0.1192, 0.9505
);
const mat3 XYZ_TO_XYZ50_M = mat3(
1.0479298208405488  ,  0.022946793341019088, -0.05019222954313557 ,
0.029627815688159344,  0.990434484573249   , -0.01707382502938514 ,
-0.009243058152591178,  0.015055144896577895,  0.7518742899580008
);
const mat3 XYZ_TO_RGB_M = mat3(
3.2406255, -1.537208 , -0.4986286,
-0.9689307,  1.8757561,  0.0415175,
0.0557101, -0.2040211,  1.0569959
);
const mat3 XYZ50_TO_XYZ_M = mat3(
0.9554734527042182  , -0.023098536874261423,  0.0632593086610217  ,
-0.028369706963208136,  1.0099954580058226  ,  0.021041398966943008,
0.012314001688319899, -0.020507696433477912,  1.3303659366080753
);
float UNCOMPAND_SRGB(float a) { return a > 0.04045 ? pow((a + 0.055) / 1.055, 2.4) : a / 12.92; }
float COMPAND_RGB(float a) { return a <= 0.0031308 ? 12.92 * a : 1.055 * pow(a, 0.41666666666) - 0.055; }
vec3 SRGB_TO_RGB(vec3 s) { return vec3(UNCOMPAND_SRGB(s.x), UNCOMPAND_SRGB(s.y), UNCOMPAND_SRGB(s.z)); }
vec3 RGB_TO_SRGB(vec3 r) { return vec3(COMPAND_RGB(r.x), COMPAND_RGB(r.y), COMPAND_RGB(r.z)); }
vec3 RGB_TO_XYZ(vec3 rgb) { return WHITE == D65_WHITE ? rgb * RGB_TO_XYZ_M : rgb * RGB_TO_XYZ_M * XYZ_TO_XYZ50_M; }
vec3 SRGB_TO_XYZ(vec3 s) { return RGB_TO_XYZ(SRGB_TO_RGB(s)); }
float XYZ_TO_LAB_F(float x) { return x > 0.00885645167 ? pow(x, 0.333333333) : 7.78703703704 * x + 0.13793103448; }
vec3 XYZ_TO_LAB(vec3 xyz) {
    vec3 x = xyz / WHITE;
    x = vec3(XYZ_TO_LAB_F(x.x), XYZ_TO_LAB_F(x.y), XYZ_TO_LAB_F(x.z));
    return vec3(116.0 * x.y - 16.0, 500.0 * (x.x - x.y), 200.0 * (x.y - x.z));
}
vec3 SRGB_TO_LAB(vec3 s) { return XYZ_TO_LAB(SRGB_TO_XYZ(s)); }
vec3 LAB_TO_LCH(vec3 Lab) { return vec3(Lab.x, sqrt(dot(Lab.yz, Lab.yz)), atan(Lab.z, Lab.y) * 57.2957795131); }
vec3 SRGB_TO_LCH(vec3 s) { return LAB_TO_LCH(SRGB_TO_LAB(s)); }
float LAB_TO_XYZ_F(float x) { return x > 0.206897 ? x * x * x : 0.12841854934 * (x - 0.137931034); }
vec3 LAB_TO_XYZ(vec3 Lab) {
    float w = (Lab.x + 16.0) / 116.0;
    return WHITE * vec3(LAB_TO_XYZ_F(w + Lab.y / 500.0), LAB_TO_XYZ_F(w), LAB_TO_XYZ_F(w - Lab.z / 200.0));
}
vec3 XYZ_TO_RGB(vec3 xyz) { return WHITE == D65_WHITE ? xyz * XYZ_TO_RGB_M : XYZ50_TO_XYZ_M * XYZ_TO_RGB_M * xyz; }
vec3 XYZ_TO_SRGB(vec3 xyz) { return RGB_TO_SRGB(XYZ_TO_RGB(xyz)); }
vec3 LCH_TO_LAB(vec3 LCh) { return vec3(LCh.x, LCh.y * cos(LCh.z * 0.01745329251), LCh.y * sin(LCh.z * 0.01745329251)); }
vec3 LCH_TO_SRGB(vec3 lch) { return XYZ_TO_SRGB(LAB_TO_XYZ(LCH_TO_LAB(lch))); }

vec4 getDispersion(sampler2D tex, vec2 uv, vec2 offset, float factor) {
    const float NR = 0.98;
    const float NG = 1.00;
    const float NB = 1.02;
    vec4 p;
    p.r = texture(tex, uv + offset * (1.0 - (NR - 1.0) * factor)).r;
    p.g = texture(tex, uv + offset * (1.0 - (NG - 1.0) * factor)).g;
    p.b = texture(tex, uv + offset * (1.0 - (NB - 1.0) * factor)).b;
    p.a = 1.0;
    return p;
}

void main() {
    vec2 inSize = InSize;
    if (inSize.x <= 0.0 || inSize.y <= 0.0) {
        inSize = vec2(textureSize(Sampler0, 0));
    }
    vec2 UV = gl_FragCoord.xy / inSize;

    vec3 base = texture(Sampler0, UV).rgb;

    vec2 pShadow = screenToUV(gl_FragCoord.xy + ShadowOffset, inSize);
    SDFResult fShadow = fieldWidgets(pShadow, inSize);
    float shadow = exp(-abs(fShadow.dist) * inSize.y / max(ShadowExpand, 1e-4)) * 0.6 * ShadowFactor;
    vec3 baseShadowed = base - vec3(shadow);

    vec2 p = screenToUV(gl_FragCoord.xy, inSize);
    SDFResult f = fieldWidgets(p, inSize);
    float merged = f.dist;

    int STEP = int(DebugStep + 0.5);

    if (STEP <= 0) {
        float px = 2.0 / inSize.y;
        vec3 col = (merged > 0.0 ? vec3(1.0) * merged : vec3(1.0) * (-merged) * 2.0) * 3.0;
        col = mix(col, vec3(1.0), 1.0 - smoothstep(0.5 / inSize.y - px, 0.5 / inSize.y + px, abs(merged)));
        fragColor = vec4(col, 1.0);
        return;
    }

    if (STEP <= 1) {
        float px = 2.0 / inSize.y;
        vec3 col = (merged > 0.0) ? vec3(0.9, 0.6, 0.3) : vec3(0.65, 0.85, 1.0);
        col *= 1.0 - exp(-0.03 * abs(merged) * inSize.y);
        col *= 0.6 + 0.4 * smoothstep(-0.5, 0.5, cos(0.25 * abs(merged) * inSize.y * 2.0));
        col = mix(col, vec3(1.0), 1.0 - smoothstep(1.5 / inSize.y - px, 1.5 / inSize.y + px, abs(merged)));
        fragColor = vec4(col, 1.0);
        return;
    }

    if (merged >= 0.0) {
        fragColor = vec4(baseShadowed, 1.0);
        return;
    }

    int idx = f.index;
    vec4 o0 = Optics0[idx];
    vec4 o1 = Optics1[idx];
    vec4 o2 = Optics2[idx];
    vec4 tint = Tints[idx];

    float refThickness = o0.x;
    float refFactor    = o0.y;
    float refDisp      = o0.z;
    float refFresRange = o0.w;

    float refFresHard  = o1.x / 100.0;
    float refFresFac   = o1.y / 100.0;
    float glareRange   = o1.z;
    float glareHard    = o1.w / 100.0;

    float glareConv    = o2.x / 100.0;
    float glareOpp     = o2.y / 100.0;
    float glareFac     = o2.z / 100.0;
    float glareAngle   = o2.w;

    vec2 normal = f.normal;
    float nlen = length(normal);
    if (nlen > 1e-6) normal /= nlen;

    if (STEP <= 2) {
        vec3 normalColor = vec2ToRgb(normal);
        fragColor = vec4(normalColor, nlen);
        return;
    }

    float nmerged = -merged * inSize.y;
    float xR = 1.0 - nmerged / max(refThickness, 1e-6);
    float thetaI = asin(pow(xR, 2.0));
    float thetaT = asin((1.0 / max(refFactor, 1e-6)) * sin(thetaI));
    float edgeFactor = -tan(thetaT - thetaI);
    if (nmerged >= refThickness) edgeFactor = 0.0;

    if (STEP <= 3) {
        fragColor = vec4(vec3(edgeFactor), 1.0);
        return;
    }

    if (STEP <= 4) {
        vec3 normalColor = vec2ToRgb(normal);
        float ring = max(edgeFactor, 0.0);
        vec3 outC = clamp(normalColor * ring * 4.0, 0.0, 1.0);
        fragColor = vec4(outC, 1.0);
        return;
    }

    if (STEP <= 5) {
        vec4 outColor = texture(Sampler1, UV);
        fragColor = outColor;
        return;
    }

    vec2 refrOffset = -normal * edgeFactor * 0.08 * vec2(inSize.y / inSize.x, 1.0);

    if (STEP <= 6) {
        vec4 blurredPixel = texture(Sampler1, UV + refrOffset);
        fragColor = blurredPixel;
        return;
    }

    float fresnelFactor = clamp(
    pow(1.0 + merged * inSize.y / 1500.0 * pow(500.0 / max(refFresRange, 1e-6), 2.0) + refFresHard, 5.0),
    0.0, 1.0
    );

    if (STEP <= 7) {
        vec4 blurredPixel = texture(Sampler1, UV + refrOffset);
        vec4 outColor = mix(blurredPixel, vec4(1.0), (fresnelFactor * refFresFac * 0.7) * nlen);
        fragColor = outColor;
        return;
    }

    if (STEP <= 8) {
        vec4 blurredPixel = texture(Sampler1, UV + refrOffset);

        float glareGeo = clamp(
        pow(1.0 + merged * inSize.y / 1500.0 * pow(500.0 / max(glareRange, 1e-6), 2.0) + glareHard, 5.0),
        0.0, 1.0
        );

        float ang = (vec2ToAngle(normalize(normal)) - PI * 0.25 + glareAngle) * 2.0;
        int farSide = 0;
        if ((ang > PI * (2.0 - 0.5) && ang < PI * (4.0 - 0.5)) || (ang < PI * (0.0 - 0.5))) farSide = 1;
        float gAngle =
        (0.5 + sin(ang) * 0.5) *
        (farSide == 1 ? 1.2 * glareOpp : 1.2) *
        glareFac;
        gAngle = clamp(pow(gAngle, 0.1 + glareConv * 2.0), 0.0, 1.0);

        vec4 outColor = mix(blurredPixel, vec4(1.0), gAngle * glareGeo);
        fragColor = outColor;
        return;
    }

    {
        vec4 dispPixel = getDispersion(Sampler1, UV, refrOffset, refDisp);

        vec4 outColor = mix(dispPixel, vec4(tint.rgb, 1.0), tint.a * 0.8);

        float fres = fresnelFactor;
        vec3 fresLCH = SRGB_TO_LCH(mix(vec3(1.0), tint.rgb, tint.a * 0.5));
        fresLCH.x += 20.0 * fres * refFresFac;
        fresLCH.x = clamp(fresLCH.x, 0.0, 100.0);
        outColor = mix(outColor, vec4(LCH_TO_SRGB(fresLCH), 1.0), fres * refFresFac * 0.7 * nlen);

        float glareGeo = clamp(
        pow(1.0 + merged * inSize.y / 1500.0 * pow(500.0 / max(glareRange, 1e-6), 2.0) + glareHard, 5.0),
        0.0, 1.0
        );

        float ang = (vec2ToAngle(normalize(normal)) - PI * 0.25 + glareAngle) * 2.0;
        int farSide = 0;
        if ((ang > PI * (2.0 - 0.5) && ang < PI * (4.0 - 0.5)) || (ang < PI * (0.0 - 0.5))) farSide = 1;
        float gAngle =
        (0.5 + sin(ang) * 0.5) *
        (farSide == 1 ? 1.2 * glareOpp : 1.2) *
        glareFac;
        gAngle = clamp(pow(gAngle, 0.1 + glareConv * 2.0), 0.0, 1.0);

        vec4 blurredPixel = texture(Sampler1, UV + refrOffset);

        vec3 glareLCH = SRGB_TO_LCH(mix(blurredPixel.rgb, tint.rgb, tint.a * 0.5));
        glareLCH.x += 150.0 * gAngle * glareGeo;
        glareLCH.y += 30.0  * gAngle * glareGeo;
        glareLCH.x = clamp(glareLCH.x, 0.0, 120.0);
        outColor = mix(outColor, vec4(LCH_TO_SRGB(glareLCH), 1.0), gAngle * glareGeo * nlen);

        vec3 finalCol = mix(outColor.rgb, baseShadowed, smoothstep(-0.001, 0.001, merged));
        fragColor = vec4(finalCol, 1.0);
    }
}