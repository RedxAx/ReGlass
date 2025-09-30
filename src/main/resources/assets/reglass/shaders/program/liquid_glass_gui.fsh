#version 150

uniform sampler2D iChannel0Sampler;
uniform sampler2D iChannel2Sampler;
uniform sampler2D iChannel3Sampler;

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

// Pixel epsilon for edge smoothing, in screen pixels
const float EPS_PIX = 2.;

// Thickness of the refractive boundary (in normalized units)
const float REFR_DIM = 0.05;

// Magnitude of the refraction distortion
const float REFR_MAG = 0.1;

// Controls chromatic aberration (color separation in refraction)
const float REFR_ABERRATION = 5.;

// Index of refraction for RGB channels (simulates dispersion)
const vec3 REFR_IOR = vec3(1.51, 1.52, 1.53);

// Width of the edge highlight/dim region
const float EDGE_DIM = .003;

// Tint color and strength (RGBA, alpha is blend factor)
const vec4 TINT_COLOR = vec4(0);

// Direction of rim lighting (normalized vector)
const vec2 RIM_LIGHT_VEC = normalize(vec2(-1., 1.));

// Rim light color and intensity (RGB + alpha for strength)
const vec4 RIM_LIGHT_COLOR = vec4(vec3(1.),.15);

// Minimum and maximum offset for reflection sampling
const float REFL_OFFSET_MIN = 0.035;
const float REFL_OFFSET_MAG = 0.005;

// Smoothing factor for blending widget fields
const float FIELD_SMOOTHING = 0.003;

#define PI 3.141592653589323

vec3 sdgCircle( in vec2 p, in float r )
{
    float l = length(p);
    vec2 n = l > 1e-6 ? p / l : vec2(0.0);
    return vec3( l-r, n );
}

vec3 sdgBox( in vec2 p, in vec2 b, vec4 ra )
{
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

vec3 sdgSMin( in vec3 a, in vec3 b, in float k )
{
    k *= 4.0;
    float h = max( k-abs(a.x-b.x), 0.0 )/(2.0*k);
    float d = min(a.x,b.x)-h*h*k;
    vec2  n = normalize(mix(a.yz,b.yz,(a.x<b.x)?h:1.0-h));
    return vec3(d, n);
}

vec3 sdgMin( in vec3 a, in vec3 b )
{
    return (a.x<b.x) ? a : b;
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

vec3 fieldWidgets(vec2 p) {
    int n = int(Count + 0.5);
    vec3 f = vec3(1e6, 0.0, 0.0);
    bool hasAny = false;

    for (int i = 0; i < MAX_WIDGETS; i++) {
        if (i >= n) break;
        vec4 rc = Rects[i];
        vec4 rr = Rads[i];


        vec2 cPx = vec2(rc.x + 0.5*rc.z, rc.y + 0.5*rc.w);
        vec2 c = screenToUV(cPx, InSize);
        vec2 b = 0.5 * vec2(rc.z, rc.w) / InSize.y;
        vec4 rad = rr / InSize.y;
        vec2 pLoc = p - c;

        vec3 g = sdgBox(pLoc, b, rad);
        if (!hasAny) {
            f = g;
            hasAny = true;
        } else {
            f = sdgSMin(f, g, FIELD_SMOOTHING);
        }
    }

    if (!hasAny) {
        return vec3(1e6, 0.0, 0.0);
    }
    return f;
}

void refractionLayer(inout vec3 col, inout Shared s) {
    float boundary = lerp(-REFR_DIM, s.EPS, s.d);
    boundary = mix(boundary, 0., smoothstep(0.,s.EPS,s.d));

    float cosBoundary = 1.0 - cos(boundary * PI / 2.0);

    vec3 ior = mix(
    vec3(REFR_IOR.g),
    REFR_IOR,
    REFR_ABERRATION
    );

    vec2 offset = -s.norm * REFR_MAG;

    vec3 ratios = pow(vec3(cosBoundary), ior);
    vec2 offsetR = offset * ratios.r;
    vec2 offsetG = offset * ratios.g;
    vec2 offsetB = offset * ratios.b;

    vec3 baseColor = texture(iChannel0Sampler, s.UV).rgb;

    float r = texture(iChannel2Sampler, s.UV + offsetR).r;
    float g = texture(iChannel2Sampler, s.UV + offsetG).g;
    float b = texture(iChannel2Sampler, s.UV + offsetB).b;
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
    vec3 reflectionColor = clamp(texture(iChannel3Sampler, s.UV + reflectionOffset).rgb, 0., 1.);
    reflectionColor = mix(reflectionColor, TINT_COLOR.rgb, TINT_COLOR.a);

    vec3 mergedEdgeColor = blendScreen(rimLight, reflectionColor);
    vec3 edgeColor = blendLighten(col, mergedEdgeColor);
    col = mix(col, edgeColor, cosEdge);
}

void main()
{
    vec2 UV = texCoord;

    float EPS = EPS_PIX / InSize.y;


    vec2 p = screenToUV(gl_FragCoord.xy, InSize);


    vec3 f = fieldWidgets(p);

    Shared sh;
    sh.EPS = EPS;
    sh.p = p;
    sh.UV = UV;
    sh.f = f;
    sh.d = f.x;
    sh.norm = f.yz;


    if (sh.d > 0.5) {
        fragColor = vec4(texture(iChannel0Sampler, UV).rgb, 1.0);
        return;
    }

    vec3 col = vec3(0.);

    refractionLayer(col, sh);
    tintLayer(col, sh);

    fragColor = vec4(col, 1.);
}