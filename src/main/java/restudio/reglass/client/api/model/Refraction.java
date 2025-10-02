package restudio.reglass.client.api.model;

import org.joml.Vector3f;

public record Refraction(
        float dimension, float minDimension,
        float magnitude, float minMagnitude,
        float chromaticAberration,
        Vector3f ior
) {
    public static final Refraction DEFAULT = new Refraction(0.05f, 0.02f, 0.1f, 0.02f, 5.0f, new Vector3f(1.51f, 1.52f, 1.53f));
}