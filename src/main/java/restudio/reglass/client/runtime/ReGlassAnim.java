package restudio.reglass.client.runtime;

import restudio.reglass.client.api.ReGlassConfig;

public final class ReGlassAnim {
    public static final ReGlassAnim INSTANCE = new ReGlassAnim();

    private boolean init;
    private float tintAlpha;
    private float smoothing;
    private float blurRadiusF;

    private float shadowExpand;
    private float shadowFactor;
    private float shadowOffsetX;
    private float shadowOffsetY;

    private float refThickness;
    private float refFactor;
    private float refDispersion;
    private float refFresnelRange;
    private float refFresnelHardness;
    private float refFresnelFactor;

    private float glareRange;
    private float glareHardness;
    private float glareConvergence;
    private float glareOppositeFactor;
    private float glareFactor;
    private float glareAngleRad;

    private float debugStep;
    private float pixelatedGridSize;

    private ReGlassAnim() {}

    public void update(ReGlassConfig cfg, double dtSeconds) {
        float tau = 0.15f;
        float a = alpha(dtSeconds, tau);
        if (!init) {
            tintAlpha = cfg.defaultTintAlpha;
            smoothing = cfg.defaultSmoothing;
            blurRadiusF = cfg.defaultBlurRadius;
            shadowExpand = cfg.defaultShadowExpand;
            shadowFactor = cfg.defaultShadowFactor;
            shadowOffsetX = cfg.defaultShadowOffsetX;
            shadowOffsetY = cfg.defaultShadowOffsetY;
            refThickness = cfg.defaultRefThickness;
            refFactor = cfg.defaultRefFactor;
            refDispersion = cfg.defaultRefDispersion;
            refFresnelRange = cfg.defaultRefFresnelRange;
            refFresnelHardness = cfg.defaultRefFresnelHardness;
            refFresnelFactor = cfg.defaultRefFresnelFactor;
            glareRange = cfg.defaultGlareRange;
            glareHardness = cfg.defaultGlareHardness;
            glareConvergence = cfg.defaultGlareConvergence;
            glareOppositeFactor = cfg.defaultGlareOppositeFactor;
            glareFactor = cfg.defaultGlareFactor;
            glareAngleRad = cfg.defaultGlareAngleRad;
            debugStep = cfg.debugStep;
            pixelatedGridSize = cfg.pixelatedGridSize;
            init = true;
            return;
        }
        tintAlpha = lerp(tintAlpha, cfg.defaultTintAlpha, a);
        smoothing = lerp(smoothing, cfg.defaultSmoothing, a);
        blurRadiusF = lerp(blurRadiusF, cfg.defaultBlurRadius, a);
        shadowExpand = lerp(shadowExpand, cfg.defaultShadowExpand, a);
        shadowFactor = lerp(shadowFactor, cfg.defaultShadowFactor, a);
        shadowOffsetX = lerp(shadowOffsetX, cfg.defaultShadowOffsetX, a);
        shadowOffsetY = lerp(shadowOffsetY, cfg.defaultShadowOffsetY, a);
        refThickness = lerp(refThickness, cfg.defaultRefThickness, a);
        refFactor = lerp(refFactor, cfg.defaultRefFactor, a);
        refDispersion = lerp(refDispersion, cfg.defaultRefDispersion, a);
        refFresnelRange = lerp(refFresnelRange, cfg.defaultRefFresnelRange, a);
        refFresnelHardness = lerp(refFresnelHardness, cfg.defaultRefFresnelHardness, a);
        refFresnelFactor = lerp(refFresnelFactor, cfg.defaultRefFresnelFactor, a);
        glareRange = lerp(glareRange, cfg.defaultGlareRange, a);
        glareHardness = lerp(glareHardness, cfg.defaultGlareHardness, a);
        glareConvergence = lerp(glareConvergence, cfg.defaultGlareConvergence, a);
        glareOppositeFactor = lerp(glareOppositeFactor, cfg.defaultGlareOppositeFactor, a);
        glareFactor = lerp(glareFactor, cfg.defaultGlareFactor, a);
        glareAngleRad = lerp(glareAngleRad, cfg.defaultGlareAngleRad, a);
        debugStep = lerp(debugStep, cfg.debugStep, a);
        pixelatedGridSize = lerp(pixelatedGridSize, cfg.pixelatedGridSize, a);
    }

    private static float alpha(double dt, float tau) {
        if (dt <= 0) return 0;
        double al = 1.0 - Math.exp(-dt / Math.max(1e-4, tau));
        return (float) al;
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public float tintAlpha() { return tintAlpha; }
    public float smoothing() { return smoothing; }
    public int blurRadiusInt() { return Math.max(0, Math.round(blurRadiusF)); }

    public float shadowExpand() { return shadowExpand; }
    public float shadowFactor() { return shadowFactor; }
    public float shadowOffsetX() { return shadowOffsetX; }
    public float shadowOffsetY() { return shadowOffsetY; }

    public float refThickness() { return refThickness; }
    public float refFactor() { return refFactor; }
    public float refDispersion() { return refDispersion; }
    public float refFresnelRange() { return refFresnelRange; }
    public float refFresnelHardness() { return refFresnelHardness; }
    public float refFresnelFactor() { return refFresnelFactor; }

    public float glareRange() { return glareRange; }
    public float glareHardness() { return glareHardness; }
    public float glareConvergence() { return glareConvergence; }
    public float glareOppositeFactor() { return glareOppositeFactor; }
    public float glareFactor() { return glareFactor; }
    public float glareAngleRad() { return glareAngleRad; }

    public float debugStep() { return debugStep; }
    public float pixelatedGridSize() { return pixelatedGridSize; }
}