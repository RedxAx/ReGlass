#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D Sampler2;

layout(std140) uniform SamplerInfo { vec2 OutSize; vec2 InSize; };
layout(std140) uniform CustomUniforms {
    float Time;
    vec4 Mouse;
    float ScreenWantsBlur;
    vec3 RIM_LIGHT_VEC;
    vec4 RIM_LIGHT_COLOR;
    float EPS_PIX;
};

#define MAX_WIDGETS 64
layout(std140) uniform WidgetInfo {
    float Count;
    vec4 Rects[MAX_WIDGETS];
    vec4 Rads[MAX_WIDGETS];
    vec4 Tints[MAX_WIDGETS];
    vec4 RefractionParams[MAX_WIDGETS];
    vec4 RefractionExtras[MAX_WIDGETS];
    vec4 ReflectionParams[MAX_WIDGETS];
    vec4 ReflectionExtras[MAX_WIDGETS];
    vec4 Smoothings[MAX_WIDGETS];
};

in vec2 texCoord;
out vec4 fragColor;

#define PI 3.141592653589793

struct SDFResult {
    float dist;
    vec2 normal;
    float aspect;
    int index;
};

float lerp(float minV, float maxV, float v) { return clamp((v - minV) / (maxV - minV), 0., 1.); }
vec2 screenToUV(vec2 screen, vec2 res) { return (screen.xy - .5 * res.xy) / res.y; }
vec3 blendScreen(vec3 a, vec3 b) { return 1. - (1. - a) * (1. - b); }
vec3 blendLighten(vec3 a, vec3 b) { return max(a, b); }

vec3 sdgBox( in vec2 p, in vec2 b, vec4 ra ) {
    ra.xy   = (p.x>0.0)?ra.xy : ra.zw;
    float r = (p.y>0.0)?ra.x  : ra.y;
    vec2 w = abs(p)-(b-r);
    vec2 s = vec2(p.x<0.0?-1:1,p.y<0.0?-1:1);
    float g = max(w.x,w.y);
    vec2  q = max(w,0.0);
    float l = length(q);
    float dist = (g>0.0)?l-r: g-r;
    vec2  n    = (g>0.0)? (q / max(l,1e-6)) : ((w.x>w.y)?vec2(1,0):vec2(0,1));
    return vec3(dist, s*n);
}

SDFResult opSmoothUnion( in SDFResult a, in SDFResult b, in float k ) {
    if (k == 0.0) {
        if (a.dist < b.dist) return a;
        return b;
    }
    float h = clamp( 0.5 + 0.5*(a.dist-b.dist)/k, 0.0, 1.0 );
    float d = mix( a.dist, b.dist, h ) - k*h*(1.0-h);
    vec2 n = normalize(mix(a.normal,b.normal,h));
    float aspect = mix(a.aspect, b.aspect, h);
    int index = a.dist < b.dist ? a.index : b.index;
    return SDFResult(d, n, aspect, index);
}

SDFResult opHardUnion(SDFResult a, SDFResult b) {
    return a.dist < b.dist ? a : b;
}

SDFResult opHardSubtract(SDFResult a, SDFResult b) {
    float d = max(a.dist, -b.dist);
    if (d == a.dist) {
        return a;
    } else {
        // New surface created from b's geometry on a's material
        return SDFResult(d, -b.normal, a.aspect, a.index);
    }
}


SDFResult fieldWidgets(vec2 p, vec2 inSize) {
    int n = int(Count + 0.5);
    if (n == 0) return SDFResult(1e6, vec2(0.0), 1.0, -1);

    SDFResult positiveShapes = SDFResult(1e6, vec2(0.0), 1.0, -1);
    bool hasPositive = false;

    // Pass 1: Union all positive/neutral shapes
    for (int i = 0; i < MAX_WIDGETS; i++) {
        if (i >= n) break;
        if (Smoothings[i].x < 0.0) continue;

        vec4 rc = Rects[i];
        vec4 rr = Rads[i];
        vec2 cPx = vec2(rc.x + 0.5*rc.z, rc.y + 0.5*rc.w);
        vec2 c = screenToUV(cPx, inSize);
        vec2 b = 0.5 * vec2(rc.z, rc.w) / inSize.y;
        vec4 rad = rr / inSize.y;
        vec3 g_vec = sdgBox(p - c, b, rad);
        float aspect = min(rc.z, rc.w) / max(rc.z, rc.w);
        SDFResult g = SDFResult(g_vec.x, g_vec.yz, aspect, i);

        if (!hasPositive) {
            positiveShapes = g;
            hasPositive = true;
        } else {
            positiveShapes = opSmoothUnion(positiveShapes, g, Smoothings[i].x);
        }
    }

    SDFResult finalField = positiveShapes;

    // Pass 2 & 3: Subtract and re-union negative (repulsive) shapes
    for (int i = 0; i < MAX_WIDGETS; i++) {
        if (i >= n) break;
        if (Smoothings[i].x >= 0.0) continue;

        vec4 rc = Rects[i];
        vec4 rr = Rads[i];
        vec2 cPx = vec2(rc.x + 0.5*rc.z, rc.y + 0.5*rc.w);
        vec2 c = screenToUV(cPx, inSize);
        vec2 b = 0.5 * vec2(rc.z, rc.w) / inSize.y;
        vec4 rad = rr / inSize.y;
        vec3 g_vec = sdgBox(p - c, b, rad);
        float aspect = min(rc.z, rc.w) / max(rc.z, rc.w);
        SDFResult g = SDFResult(g_vec.x, g_vec.yz, aspect, i);

        float repulsion = -Smoothings[i].x;
        SDFResult g_expanded = SDFResult(g.dist - repulsion, g.normal, g.aspect, g.index);

        finalField = opHardSubtract(finalField, g_expanded);
        finalField = opHardUnion(finalField, g);
    }

    return finalField;
}

struct Shared {
    float EPS;
    vec2 p;
    vec2 UV;
    SDFResult f;
    float dynamicRefrDim;
    float dynamicRefrMag;
    float dynamicEdgeDim;
    float dynamicReflOffsetMin;
    float dynamicReflOffsetMag;
};

void baseLayer(out vec3 col, in Shared s) {
    vec4 refrParams = RefractionParams[s.f.index];
    float REFR_DIM = refrParams.x;
    float REFR_MAG = refrParams.y;
    float MIN_REFR_DIM = refrParams.z;
    float MIN_REFR_MAG = refrParams.w;

    vec4 refrExtras = RefractionExtras[s.f.index];
    float REFR_ABERRATION = refrExtras.x;
    vec3 REFR_IOR = refrExtras.yzw;

    s.dynamicRefrDim = mix(MIN_REFR_DIM, REFR_DIM, s.f.aspect);
    s.dynamicRefrMag = mix(MIN_REFR_MAG, REFR_MAG, s.f.aspect);

    float boundary = lerp(-s.dynamicRefrDim, s.EPS, s.f.dist);
    boundary = mix(boundary, 0., smoothstep(0.,s.EPS,s.f.dist));
    float cosBoundary = 1.0 - cos(boundary * PI / 2.0);
    vec3 ior = mix(vec3(REFR_IOR.g), REFR_IOR, REFR_ABERRATION);
    vec2 offset = -s.f.normal * s.dynamicRefrMag;
    vec3 ratios = pow(vec3(cosBoundary), ior);
    vec2 offsetR = offset * ratios.r;
    vec2 offsetG = offset * ratios.g;
    vec2 offsetB = offset * ratios.b;

    float r = texture(Sampler1, s.UV + offsetR).r;
    float g = texture(Sampler1, s.UV + offsetG).g;
    float b = texture(Sampler1, s.UV + offsetB).b;
    col = vec3(r,g,b);
}

void tintLayer(inout vec3 col, in Shared s) {
    vec4 TINT_COLOR = Tints[s.f.index];

    vec4 reflParams = ReflectionParams[s.f.index];
    float EDGE_DIM = reflParams.x;
    float REFL_OFFSET_MIN = reflParams.y;
    float REFL_OFFSET_MAG = reflParams.z;

    vec4 reflExtras = ReflectionExtras[s.f.index];
    float MIN_EDGE_DIM = reflExtras.x;
    float MIN_REFL_OFFSET_MIN = reflExtras.y;
    float MIN_REFL_OFFSET_MAG = reflExtras.z;

    s.dynamicEdgeDim = mix(MIN_EDGE_DIM, EDGE_DIM, s.f.aspect);
    s.dynamicReflOffsetMin = mix(MIN_REFL_OFFSET_MIN, REFL_OFFSET_MIN, s.f.aspect);
    s.dynamicReflOffsetMag = mix(MIN_REFL_OFFSET_MAG, REFL_OFFSET_MAG, s.f.aspect);

    float interior = smoothstep(s.EPS, 0., s.f.dist);
    col = mix(col, TINT_COLOR.rgb, TINT_COLOR.a * interior);
    float a = smoothstep(s.EPS, 0., s.f.dist);
    float b = lerp(-s.dynamicEdgeDim, 0., s.f.dist);
    float edge = min(a, b);
    float cosEdge = 1. - cos(edge * PI / 2.);
    float rimLightIntensity = abs(dot(normalize(s.f.normal), RIM_LIGHT_VEC.xy));
    vec3 rimLight = RIM_LIGHT_COLOR.rgb * RIM_LIGHT_COLOR.a * rimLightIntensity;
    vec2 reflectionOffset = (s.dynamicReflOffsetMin + s.dynamicReflOffsetMag * cosEdge) * s.f.normal;

    vec3 reflectionColor = clamp(texture(Sampler2, s.UV + reflectionOffset).rgb, 0., 1.);
    reflectionColor = mix(reflectionColor, TINT_COLOR.rgb, TINT_COLOR.a);
    vec3 mergedEdgeColor = blendScreen(rimLight, reflectionColor);
    vec3 edgeColor = blendLighten(col, mergedEdgeColor);
    col = mix(col, edgeColor, cosEdge);
}

void main()
{
    vec2 inSize = InSize;
    if (inSize.x <= 0.0 || inSize.y <= 0.0) { inSize = vec2(textureSize(Sampler0, 0)); }
    vec2 UV = gl_FragCoord.xy / inSize;
    float EPS = EPS_PIX / inSize.y;
    vec2 p = screenToUV(gl_FragCoord.xy, inSize);

    SDFResult f = fieldWidgets(p, inSize);

    vec3 backgroundColor;
    if (ScreenWantsBlur > 0.5) {
        backgroundColor = texture(Sampler1, UV).rgb;
    } else {
        backgroundColor = texture(Sampler0, UV).rgb;
    }

    float opacity = smoothstep(EPS, -EPS, f.dist);
    if (opacity <= 0.0 || f.index < 0) {
        fragColor = vec4(backgroundColor, 1.0);
        return;
    }

    Shared sh;
    sh.EPS = EPS; sh.p = p; sh.UV = UV; sh.f = f;

    vec3 col;
    baseLayer(col, sh);
    tintLayer(col, sh);

    col = mix(backgroundColor, col, opacity);

    fragColor = vec4(col, 1.0);
}