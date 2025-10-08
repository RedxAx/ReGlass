package restudio.reglass.client.api;

import java.util.Optional;
import restudio.reglass.client.api.model.Optics;
import restudio.reglass.client.api.model.Smoothing;
import restudio.reglass.client.api.model.Tint;

public class WidgetStyle {
    Optional<Tint> tint = Optional.empty();
    Optional<Smoothing> smoothing = Optional.empty();
    Optional<Optics> optics = Optional.empty();

    public static WidgetStyle create() {
        return new WidgetStyle();
    }

    public WidgetStyle tint(Tint tint) {
        this.tint = Optional.of(tint);
        return this;
    }

    public WidgetStyle smoothing(Smoothing smoothing) {
        this.smoothing = Optional.of(smoothing);
        return this;
    }

    public WidgetStyle optics(Optics optics) {
        this.optics = Optional.of(optics);
        return this;
    }

    public Tint getTint() {
        return tint.orElse(ReGlassConfig.INSTANCE.tint);
    }

    public Smoothing getSmoothing() {
        return smoothing.orElse(ReGlassConfig.INSTANCE.smoothing);
    }

    public Optics getOptics() {
        return optics.orElse(Optics.DEFAULT);
    }
}