package restudio.reglass.client.api.model;

import org.joml.Vector2f;

public record RimLight(Vector2f direction, int color, float intensity) {
    public static final RimLight DEFAULT = new RimLight(new Vector2f(-1.0f, 1.0f).normalize(), 0xFFFFFF, 0.1f);
}