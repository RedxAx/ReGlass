package restudio.reglass.client.api.model;

public record Smoothing(float factor) {
    public static final Smoothing DEFAULT = new Smoothing(0.003f);
}