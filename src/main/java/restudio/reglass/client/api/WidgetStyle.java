package restudio.reglass.client.api;

import java.util.Optional;
import restudio.reglass.client.api.model.Edge;
import restudio.reglass.client.api.model.Reflection;
import restudio.reglass.client.api.model.Refraction;
import restudio.reglass.client.api.model.RimLight;
import restudio.reglass.client.api.model.Tint;

public class WidgetStyle {
    Optional<Tint> tint = Optional.empty();
    Optional<Refraction> refraction = Optional.empty();
    Optional<Reflection> reflection = Optional.empty();
    Optional<Edge> edge = Optional.empty();
    Optional<RimLight> rimLight = Optional.empty();

    public static WidgetStyle create() {
        return new WidgetStyle();
    }

    public WidgetStyle tint(Tint tint) {
        this.tint = Optional.of(tint);
        return this;
    }

    public WidgetStyle refraction(Refraction refraction) {
        this.refraction = Optional.of(refraction);
        return this;
    }

    public WidgetStyle reflection(Reflection reflection) {
        this.reflection = Optional.of(reflection);
        return this;
    }

    public WidgetStyle edge(Edge edge) {
        this.edge = Optional.of(edge);
        return this;
    }

    public WidgetStyle rimLight(RimLight rimLight) {
        this.rimLight = Optional.of(rimLight);
        return this;
    }

    public Tint getTint() {
        return tint.orElse(ReGlassConfig.INSTANCE.tint);
    }

    public Refraction getRefraction() {
        return refraction.orElse(ReGlassConfig.INSTANCE.refraction);
    }

    public Reflection getReflection() {
        return reflection.orElse(ReGlassConfig.INSTANCE.reflection);
    }

    public Edge getEdge() {
        return edge.orElse(ReGlassConfig.INSTANCE.edge);
    }

    public RimLight getRimLight() {
        return rimLight.orElse(ReGlassConfig.INSTANCE.rimLight);
    }
}