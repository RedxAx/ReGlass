package restudio.reglass.client.api;

import restudio.reglass.client.api.model.Edge;
import restudio.reglass.client.api.model.Reflection;
import restudio.reglass.client.api.model.Refraction;
import restudio.reglass.client.api.model.RimLight;
import restudio.reglass.client.api.model.Tint;

public class ReGlassConfig {
    public static final ReGlassConfig INSTANCE = new ReGlassConfig();

    public Tint tint = Tint.DEFAULT;
    public Refraction refraction = Refraction.DEFAULT;
    public Reflection reflection = Reflection.DEFAULT;
    public Edge edge = Edge.DEFAULT;
    public RimLight rimLight = RimLight.DEFAULT;

    public float fieldSmoothing = 0.003f;
    public float pixelEpsilon = 2.0f;

    private ReGlassConfig() {}
}