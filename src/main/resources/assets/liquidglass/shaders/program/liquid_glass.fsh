#version 150

uniform sampler2D iChannel0;
uniform sampler2D iChannel2;
uniform sampler2D iChannel3;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform CustomUniforms {
    float Time;
    vec4 Mouse;
};

in vec2 texCoord;
out vec4 fragColor;

const float EPS_PIX = 2.;
const float REFR_DIM = 0.05;
const float REFR_MAG = 0.1;
const float REFR_ABERRATION = 5.;
const vec3 REFR_IOR = vec3(1.51, 1.52, 1.53);
const float EDGE_DIM = .003;
const vec4 TINT_COLOR = vec4(0, .6, 1, .5);
const int BLUR_AMOUNT = 30;
const vec2 RIM_LIGHT_VEC = normalize(vec2(-1., 1.));
const vec4 RIM_LIGHT_COLOR = vec4(vec3(1.),.15);
const float REFL_OFFSET_MIN = 0.035;
const float REFL_OFFSET_MAG = 0.005;
const float FIELD_SMOOTHING = 0.03;

#define FIELD_BLOBS
#define PI 3.141592653589323

vec3 sdgCircle( in vec2 p, in float r )
{
    float l = length(p);
    return vec3( l-r, p/l );
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

    return vec3(   (g>0.0)?l-r: g-r,
    s*((g>0.0)?q/l : ((w.x>w.y)?vec2(1,0):vec2(0,1))));
}

vec3 sdgBox( in vec2 p, in vec2 b, in float r )
{
    return sdgBox(p, b, vec4(r));
}

vec3 sdgSMin( in vec3 a, in vec3 b, in float k )
{
    k *= 4.0;
    float h = max( k-abs(a.x-b.x), 0.0 )/(2.0*k);
    return vec3( min(a.x,b.x)-h*h*k,
    mix(a.yz,b.yz,(a.x<b.x)?h:1.0-h) );
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

vec3 fieldBlobs(vec2 p) {
    const float SPEED = 13.;
    vec2 offset = .3 * vec2(
    sin(SPEED * Time / 9.) * .5,
    cos(SPEED * Time / 13.)
    );
    if (Mouse.z > 0.) {
        offset = screenToUV(Mouse.xy, InSize);
    }

    vec3 f = sdgCircle(p - offset, 0.1);
    f = sdgSMin(f, sdgCircle(p, 0.1), FIELD_SMOOTHING);
    f = sdgSMin(f, sdgBox(p - vec2(-.4, 0), vec2(.2, .1), .1), FIELD_SMOOTHING);
    f = sdgSMin(f, sdgBox(p - vec2(.4, 0), vec2(.2, .1), .1), FIELD_SMOOTHING);

    return f;
}

vec3 fieldRects(vec2 p) {
    vec3 r = sdgBox(p - vec2(-.12,0), vec2(0.5,0.1),.1);
    vec3 c = sdgCircle(p - vec2(0.52, 0.), 0.1);
    vec3 f = sdgMin(r,c);
    return f;
}

vec3 field(vec2 p) {
    #ifdef FIELD_BLOBS
    return fieldBlobs(p);
    #endif
    #ifdef FIELD_RECTS
    return fieldRects(p);
    #endif
    return vec3(1,0,0);
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

    vec3 baseColor = texture(iChannel0, s.UV).rgb;

    float r = texture(iChannel2, s.UV + offsetR).r;
    float g = texture(iChannel2, s.UV + offsetG).g;
    float b = texture(iChannel2, s.UV + offsetB).b;
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
    vec3 reflectionColor = clamp(texture(iChannel3, s.UV + reflectionOffset).rgb, 0., 1.);
    reflectionColor = mix(reflectionColor, TINT_COLOR.rgb, TINT_COLOR.a);

    vec3 mergedEdgeColor = blendScreen(rimLight, reflectionColor);
    vec3 edgeColor = blendLighten(col, mergedEdgeColor);
    col = mix(col, edgeColor, cosEdge);
}

void main()
{
    vec2 UV = texCoord;

    float EPS = EPS_PIX / InSize.y;
    vec2 p = screenToUV(texCoord * InSize, InSize);
    vec3 f = field(p);

    Shared sh;
    sh.EPS = EPS;
    sh.p = p;
    sh.UV = UV;
    sh.f = f;
    sh.d = f.x;
    sh.norm = f.yz;

    vec3 col = vec3(0.);

    refractionLayer(col, sh);
    tintLayer(col, sh);

    fragColor = vec4(col, 1.);
}