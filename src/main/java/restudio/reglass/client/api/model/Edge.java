package restudio.reglass.client.api.model;

public record Edge(float dimension, float minDimension) {
    public static final Edge DEFAULT = new Edge(0.003f, 0.001f);
}