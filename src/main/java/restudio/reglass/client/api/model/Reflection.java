package restudio.reglass.client.api.model;

public record Reflection(float offsetMin, float minOffsetMin, float offsetMagnitude, float minOffsetMagnitude) {
    public static final Reflection DEFAULT = new Reflection(0.035f, 0.01f, 0.005f, 0.001f);
}