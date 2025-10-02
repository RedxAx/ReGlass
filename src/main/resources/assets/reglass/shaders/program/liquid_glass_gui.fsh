#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D Sampler2;

layout(std140) uniform SamplerInfo { vec2 OutSize; vec2 InSize; };
layout(std140) uniform CustomUniforms { float Time; vec4 Mouse; float ScreenWantsBlur; };
#define MAX_WIDGETS 64
layout(std140) uniform WidgetInfo { float Count; vec4 Rects[MAX_WIDGETS]; vec4 Rads[MAX_WIDGETS]; };

in vec2 texCoord;
out vec4 fragColor;

const float REFR_DIM = 0.05;
const float REFR_MAG = 0.1;
const float EDGE_DIM = .003;
const float REFL_OFFSET_MIN = 0.035;
const float REFL_OFFSET_MAG = 0.005;

const float MIN_REFR_DIM = 0.02;
const float MIN_REFR_MAG = 0.02;
const float MIN_EDGE_DIM = .001;
const float MIN_REFL_OFFSET_MIN = 0.01;
const float MIN_REFL_OFFSET_MAG = 0.001;

const float EPS_PIX = 2.;
const float REFR_ABERRATION = 5.;
const vec3  REFR_IOR = vec3(1.51, 1.52, 1.53);
const vec4  TINT_COLOR = vec4(0);
const vec2  RIM_LIGHT_VEC = normalize(vec2(-1., 1.));
const vec4  RIM_LIGHT_COLOR = vec4(vec3(1.), .15);
const float FIELD_SMOOTHING = 0.003;
#define PI 3.141592653589793

struct SDFResult {
    float dist;
    vec2 normal;
    float aspect;
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

SDFResult sdgSMin( in SDFResult a, in SDFResult b, in float k ) {
    k *= 4.0;
    float h = max( k-abs(a.dist-b.dist), 0.0 )/(2.0*k);
    float d = min(a.dist,b.dist)-h*h*k;
    vec2  n = normalize(mix(a.normal,b.normal,(a.dist<b.dist)?h:1.0-h));
    float aspect = mix(a.aspect, b.aspect, (a.dist<b.dist)?h:1.0-h);
    return SDFResult(d, n, aspect);
}

SDFResult fieldWidgets(vec2 p, vec2 inSize) {
    int n = int(Count + 0.5);
    SDFResult f = SDFResult(1e6, vec2(0.0), 1.0);
    bool hasAny = false;

    for (int i = 0; i < MAX_WIDGETS; i++) {
        if (i >= n) break;
        vec4 rc = Rects[i];
        vec4 rr = Rads[i];

        vec2 cPx = vec2(rc.x + 0.5*rc.z, rc.y + 0.5*rc.w);
        vec2 c = screenToUV(cPx, inSize);
        vec2 b = 0.5 * vec2(rc.z, rc.w) / inSize.y;
        vec4 rad = rr / inSize.y;
        vec2 pLoc = p - c;

        vec3 g_vec = sdgBox(pLoc, b, rad);

        float aspect = min(rc.z, rc.w) / max(rc.z, rc.w);
        SDFResult g = SDFResult(g_vec.x, g_vec.yz, aspect);

        if (!hasAny) {
            f = g;
            hasAny = true;
        } else {
            f = sdgSMin(f, g, FIELD_SMOOTHING);
        }
    }
    return f;
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
    float interior = smoothstep(s.EPS, 0., s.f.dist);
    col = mix(col, TINT_COLOR.rgb, TINT_COLOR.a * interior);
    float a = smoothstep(s.EPS, 0., s.f.dist);
    float b = lerp(-s.dynamicEdgeDim, 0., s.f.dist);
    float edge = min(a, b);
    float cosEdge = 1. - cos(edge * PI / 2.);
    float rimLightIntensity = abs(dot(normalize(s.f.normal), RIM_LIGHT_VEC));
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
    if (opacity <= 0.0) {
        fragColor = vec4(backgroundColor, 1.0);
        return;
    }

    Shared sh;
    sh.EPS = EPS; sh.p = p; sh.UV = UV; sh.f = f;

    float aspectFactor = f.aspect;
    sh.dynamicRefrDim = mix(MIN_REFR_DIM, REFR_DIM, aspectFactor);
    sh.dynamicRefrMag = mix(MIN_REFR_MAG, REFR_MAG, aspectFactor);
    sh.dynamicEdgeDim = mix(MIN_EDGE_DIM, EDGE_DIM, aspectFactor);
    sh.dynamicReflOffsetMin = mix(MIN_REFL_OFFSET_MIN, REFL_OFFSET_MIN, aspectFactor);
    sh.dynamicReflOffsetMag = mix(MIN_REFL_OFFSET_MAG, REFL_OFFSET_MAG, aspectFactor);

    vec3 col;
    baseLayer(col, sh);
    tintLayer(col, sh);

    col = mix(backgroundColor, col, opacity);

    fragColor = vec4(col, 1.0);
}