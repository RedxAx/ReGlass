package restudio.reglass.client.api.model;

public record Optics(
        float refThickness,
        float refFactor,
        float refDispersion,
        float refFresnelRange,
        float refFresnelHardness,
        float refFresnelFactor,
        float glareRange,
        float glareHardness,
        float glareConvergence,
        float glareOppositeFactor,
        float glareFactor,
        float glareAngleRad
) {
    public static final Optics DEFAULT = new Optics(
            20.0f,
            1.4f,
            7.0f,
            30.0f,
            20f,
            20f,
            30.0f,
            20f,
            50f,
            80f,
            90f,
            (float)(-45.0 * Math.PI / 180.0)
    );
}