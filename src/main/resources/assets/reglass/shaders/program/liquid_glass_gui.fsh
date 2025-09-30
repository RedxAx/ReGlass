#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D Sampler2;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform CustomUniforms {
    float Time;
    vec4 Mouse;
};

#define MAX_WIDGETS 64
layout(std140) uniform WidgetInfo {
    float Count;
    vec4 Rects[MAX_WIDGETS];
    vec4 Rads[MAX_WIDGETS];
};

in vec2 texCoord;
out vec4 fragColor;

const float EPS_PIX = 2.;
const float REFR_DIM = 0.05;
const float REFR_MAG = 0.1;
const float REFR_ABERRATION = 5.;
const vec3  REFR_IOR = vec3(1.51, 1.52, 1.53);
const float EDGE_DIM = .003;
const vec4  TINT_COLOR = vec4(0);
const vec2  RIM_LIGHT_VEC = normalize(vec2(-1., 1.));
const vec4  RIM_LIGHT_COLOR = vec4(vec3(1.), .15);
const float REFL_OFFSET_MIN = 0.035;
const float REFL_OFFSET_MAG = 0.005;
const float FIELD_SMOOTHING = 0.003;

#define PI 3.141592653589793

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

vec3 sdgSMin( in vec3 a, in vec3 b, in float k ) {
    k *= 4.0;
    float h = max( k-abs(a.x-b.x), 0.0 )/(2.0*k);
    float d = min(a.x,b.x)-h*h*k;
    vec2  n = normalize(mix(a.yz,b.yz,(a.x<b.x)?h:1.0-h));
    return vec3(d, n);
}

float lerp(float minV, float maxV, float v) {
    return clamp((v - minV) / (maxV - minV), 0., 1.);
}

vec2 screenToUV(vec2 screen, vec2 res) {
    return (screen.xy - .5 * res.xy) / res.y;
}

vec3 blendScreen(vec3 a, vec3 b) {
    return 1. - (1. - a) * (1. - b);
}

vec3 blendLighten(vec3 a, vec3 b) {
    return max(a, b);
}

struct Shared {
    float EPS;
    vec2 p;
    vec2 UV;
    vec3 f;
    float d;
    vec2 norm;
};

vec3 fieldWidgets(vec2 p, vec2 inSize) {
    int n = int(Count + 0.5);
    vec3 f = vec3(1e6, 0.0, 0.0);
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

        vec3 g = sdgBox(pLoc, b, rad);
        if (!hasAny) {
            f = g;
            hasAny = true;
        } else {
            f = sdgSMin(f, g, FIELD_SMOOTHING);
        }
    }

    if (!hasAny) return vec3(1e6, 0.0, 0.0);
    return f;
}

void refractionLayer(inout vec3 col, inout Shared s) {
    float boundary = lerp(-REFR_DIM, s.EPS, s.d);
    boundary = mix(boundary, 0., smoothstep(0., s.EPS, s.d));
    float cosBoundary = 1.0 - cos(boundary * PI / 2.0);

    vec3 ior = mix(vec3(REFR_IOR.g), REFR_IOR, REFR_ABERRATION);

    vec2 offset = -s.norm * REFR_MAG;
    vec3 ratios = pow(vec3(cosBoundary), ior);

    vec2 offsetR = offset * ratios.r;
    vec2 offsetG = offset * ratios.g;
    vec2 offsetB = offset * ratios.b;

    vec3 baseColor = texture(Sampler0, s.UV).rgb;

    float r = texture(Sampler1, s.UV + offsetR).r;
    float g = texture(Sampler1, s.UV + offsetG).g;
    float b = texture(Sampler1, s.UV + offsetB).b;
    vec3 blurWarped = vec3(r,g,b);

    col = mix(baseColor, blurWarped, smoothstep(s.EPS, 0., s.d));
}

void tintLayer(inout vec3 col, inout Shared s) {
    float interior = smoothstep(s.EPS, 0., s.d);
    col = mix(col, TINT_COLOR.rgb, TINT_COLOR.a * interior);

    float a = smoothstep(s.EPS, 0., s.d);
    float b = lerp(-EDGE_DIM, 0., s.d);
    float edge = min(a, b);
    float cosEdge = 1. - cos(edge * PI / 2.);

    float rimLightIntensity = abs(dot(normalize(s.norm), RIM_LIGHT_VEC));
    vec3 rimLight = RIM_LIGHT_COLOR.rgb * RIM_LIGHT_COLOR.a * rimLightIntensity;

    vec2 reflectionOffset = (REFL_OFFSET_MIN + REFL_OFFSET_MAG * cosEdge) * s.norm;
    vec3 reflectionColor = clamp(texture(Sampler2, s.UV + reflectionOffset).rgb, 0., 1.);
    reflectionColor = mix(reflectionColor, TINT_COLOR.rgb, TINT_COLOR.a);

    vec3 mergedEdgeColor = blendScreen(rimLight, reflectionColor);
    vec3 edgeColor = blendLighten(col, mergedEdgeColor);
    col = mix(col, edgeColor, cosEdge);
}

void main()
{
    vec2 inSize = InSize;
    if (inSize.x <= 0.0 || inSize.y <= 0.0) {
        inSize = vec2(textureSize(Sampler0, 0));
    }

    float EPS = EPS_PIX / inSize.y;
    vec2 UV  = gl_FragCoord.xy / inSize;
    vec2 p   = screenToUV(gl_FragCoord.xy, inSize);

    if (Count < 0.5) {
        vec3 base = texture(Sampler0, UV).rgb;
        fragColor = vec4(mix(base, vec3(1.0, 0.0, 1.0), 0.25), 1.0);
        return;
    }

    vec3 f = fieldWidgets(p, inSize);

    Shared sh;
    sh.EPS = EPS;
    sh.p = p;
    sh.UV = UV;
    sh.f = f;
    sh.d = f.x;
    sh.norm = f.yz;

    if (sh.d > 0.5) {
        vec3 base = texture(Sampler0, UV).rgb;
        fragColor = vec4(base, 1.0);
        return;
    }

    vec3 col = vec3(0.);
    refractionLayer(col, sh);
    tintLayer(col, sh);

    fragColor = vec4(col, 1.);
}