package restudio.reglass.client.api;

import restudio.reglass.client.api.model.RimLight;
import restudio.reglass.client.api.model.Smoothing;
import restudio.reglass.client.api.model.Tint;

public class ReGlassConfig {
    public static final ReGlassConfig INSTANCE = new ReGlassConfig();

    public Tint tint = Tint.DEFAULT;
    public RimLight rimLight = RimLight.DEFAULT;
    public Smoothing smoothing = Smoothing.DEFAULT;

    public float pixelEpsilon = 2.0f;
    public int blurRadius = 3;

    public float shadowExpand = 25.0f;
    public float shadowFactor = 0.15f;
    public float shadowOffsetX = 0.0f;
    public float shadowOffsetY = 2.0f;

    public float debugStep = 9.0f;

    private ReGlassConfig() {}
}