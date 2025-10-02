package restudio.reglass.client.api.model;

public record Tint(int color, float alpha) {
    public static final Tint DEFAULT = new Tint(0x000000, 0.0f);

    public Tint(int color) {
        this(color, 0.0f);
    }
}