package restudio.reglass.client.api;

import restudio.reglass.client.runtime.ReGlassAnim;

public class WidgetStyle {

    private boolean hasTint;
    private int tintColor;
    private float tintAlpha;

    private boolean hasSmoothing;
    private float smoothingFactor;

    private boolean hasBlurRadius;
    private int blurRadius;

    private boolean hasShadow;
    private float shadowExpand;
    private float shadowFactor;
    private float shadowOffsetX;
    private float shadowOffsetY;
    private int shadowColor;
    private float shadowColorAlpha;

    private boolean hasRefraction;
    private float refThickness;
    private float refFactor;
    private float refDispersion;
    private float refFresnelRange;
    private float refFresnelHardness;
    private float refFresnelFactor;

    private boolean hasGlare;
    private float glareRange;
    private float glareHardness;
    private float glareConvergence;
    private float glareOppositeFactor;
    private float glareFactor;
    private float glareAngleRad;

    public static WidgetStyle create() { return new WidgetStyle(); }

    public WidgetStyle tint(int color, float alpha) { this.hasTint = true; this.tintColor = color; this.tintAlpha = alpha; return this; }
    public WidgetStyle smoothing(float factor) { this.hasSmoothing = true; this.smoothingFactor = factor; return this; }
    public WidgetStyle blurRadius(int radius) { this.hasBlurRadius = true; this.blurRadius = Math.max(0, radius); return this; }
    public WidgetStyle shadow(float expand, float factor, float offsetX, float offsetY) { this.hasShadow = true; this.shadowExpand = expand; this.shadowFactor = factor; this.shadowOffsetX = offsetX; this.shadowOffsetY = offsetY; return this; }
    public WidgetStyle shadowColor(int color, float alpha) { this.hasShadow = true; this.shadowColor = color; this.shadowColorAlpha = alpha; return this; }
    public WidgetStyle refractionThickness(float v) { this.hasRefraction = true; this.refThickness = v; return this; }
    public WidgetStyle refractionFactor(float v) { this.hasRefraction = true; this.refFactor = v; return this; }
    public WidgetStyle refractionDispersion(float v) { this.hasRefraction = true; this.refDispersion = v; return this; }
    public WidgetStyle fresnelRange(float v) { this.hasRefraction = true; this.refFresnelRange = v; return this; }
    public WidgetStyle fresnelHardness(float v) { this.hasRefraction = true; this.refFresnelHardness = v; return this; }
    public WidgetStyle fresnelFactor(float v) { this.hasRefraction = true; this.refFresnelFactor = v; return this; }
    public WidgetStyle glareRange(float v) { this.hasGlare = true; this.glareRange = v; return this; }
    public WidgetStyle glareHardness(float v) { this.hasGlare = true; this.glareHardness = v; return this; }
    public WidgetStyle glareConvergence(float v) { this.hasGlare = true; this.glareConvergence = v; return this; }
    public WidgetStyle glareOppositeFactor(float v) { this.hasGlare = true; this.glareOppositeFactor = v; return this; }
    public WidgetStyle glareFactor(float v) { this.hasGlare = true; this.glareFactor = v; return this; }
    public WidgetStyle glareAngleRad(float v) { this.hasGlare = true; this.glareAngleRad = v; return this; }

    public int getTintColor() { return hasTint ? tintColor : ReGlassConfig.INSTANCE.defaultTintColor; }
    public float getTintAlpha() { return hasTint ? tintAlpha : ReGlassAnim.INSTANCE.tintAlpha(); }

    public float getSmoothing() { return hasSmoothing ? smoothingFactor : ReGlassAnim.INSTANCE.smoothing(); }
    public int getBlurRadius() { return hasBlurRadius ? blurRadius : ReGlassAnim.INSTANCE.blurRadiusInt(); }

    public float getShadowExpand() { return hasShadow ? shadowExpand : ReGlassAnim.INSTANCE.shadowExpand(); }
    public float getShadowFactor() { return hasShadow ? shadowFactor : ReGlassAnim.INSTANCE.shadowFactor(); }
    public float getShadowOffsetX() { return hasShadow ? shadowOffsetX : ReGlassAnim.INSTANCE.shadowOffsetX(); }
    public float getShadowOffsetY() { return hasShadow ? shadowOffsetY : ReGlassAnim.INSTANCE.shadowOffsetY(); }
    public int getShadowColor() { return hasShadow ? shadowColor : ReGlassConfig.INSTANCE.defaultShadowColor; }
    public float getShadowColorAlpha() { return hasShadow ? shadowColorAlpha : ReGlassConfig.INSTANCE.defaultShadowColorAlpha; }

    public float getRefThickness() { return hasRefraction ? refThickness : ReGlassAnim.INSTANCE.refThickness(); }
    public float getRefFactor() { return hasRefraction ? refFactor : ReGlassAnim.INSTANCE.refFactor(); }
    public float getRefDispersion() { return hasRefraction ? refDispersion : ReGlassAnim.INSTANCE.refDispersion(); }
    public float getRefFresnelRange() { return hasRefraction ? refFresnelRange : ReGlassAnim.INSTANCE.refFresnelRange(); }
    public float getRefFresnelHardness() { return hasRefraction ? refFresnelHardness : ReGlassAnim.INSTANCE.refFresnelHardness(); }
    public float getRefFresnelFactor() { return hasRefraction ? refFresnelFactor : ReGlassAnim.INSTANCE.refFresnelFactor(); }

    public float getGlareRange() { return hasGlare ? glareRange : ReGlassAnim.INSTANCE.glareRange(); }
    public float getGlareHardness() { return hasGlare ? glareHardness : ReGlassAnim.INSTANCE.glareHardness(); }
    public float getGlareConvergence() { return hasGlare ? glareConvergence : ReGlassAnim.INSTANCE.glareConvergence(); }
    public float getGlareOppositeFactor() { return hasGlare ? glareOppositeFactor : ReGlassAnim.INSTANCE.glareOppositeFactor(); }
    public float getGlareFactor() { return hasGlare ? glareFactor : ReGlassAnim.INSTANCE.glareFactor(); }
    public float getGlareAngleRad() { return hasGlare ? glareAngleRad : ReGlassAnim.INSTANCE.glareAngleRad(); }
}