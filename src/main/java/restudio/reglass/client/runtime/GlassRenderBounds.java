package restudio.reglass.client.runtime;

/** Conservative pixel bounds for glass geometry and imperceptible shadow tails. */
public final class GlassRenderBounds {
    public static final double SHADOW_CUTOFF = 0.00001;

    private GlassRenderBounds() {}

    public record Bounds(float left, float bottom, float right, float top) {
        public Bounds clip(Bounds scissor) {
            return new Bounds(Math.max(left, scissor.left), Math.max(bottom, scissor.bottom),
                    Math.min(right, scissor.right), Math.min(top, scissor.top));
        }

        public Bounds union(Bounds other) {
            if (empty()) return other;
            if (other.empty()) return this;
            return new Bounds(Math.min(left, other.left), Math.min(bottom, other.bottom),
                    Math.max(right, other.right), Math.max(top, other.top));
        }

        public boolean empty() { return right <= left || top <= bottom; }
    }

    public static Bounds shape(float x, float y, float width, float height, float padding) {
        return new Bounds(x - padding, y - padding, x + width + padding, y + height + padding);
    }

    public static Bounds shadow(float x, float y, float width, float height,
                                float expand, float factor, float alpha, float offsetX, float offsetY) {
        double strength = Math.max(0.0, (double) factor * alpha * 0.6);
        if (strength <= SHADOW_CUTOFF) return new Bounds(0, 0, 0, 0);
        float padding = (float) (Math.max(expand, 0.0001f) * Math.log(strength / SHADOW_CUTOFF));
        // The shader shifts the evaluation point by the offset, so its footprint shifts oppositely.
        return shape(x - offsetX, y - offsetY, width, height, padding);
    }
}
