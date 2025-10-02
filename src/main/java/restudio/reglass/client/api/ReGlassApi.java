package restudio.reglass.client.api;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import restudio.reglass.client.gui.LiquidGlassGuiElementRenderState;

public final class ReGlassApi {
    private ReGlassApi() {}

    public static ReGlassConfig getGlobalConfig() {
        return ReGlassConfig.INSTANCE;
    }

    public static Builder create(DrawContext context) {
        return new Builder(context);
    }

    public static class Builder {
        private final DrawContext context;
        private int x, y, width, height;
        private float cornerRadius = -1f;
        @Nullable private Text text = null;
        private WidgetStyle style = new WidgetStyle();

        private Builder(DrawContext context) {
            this.context = context;
        }

        public Builder fromWidget(ClickableWidget widget) {
            this.position(widget.getX(), widget.getY());
            this.size(widget.getWidth(), widget.getHeight());
            this.text(widget.getMessage());
            return this;
        }

        public Builder position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder dimensions(int x, int y, int width, int height) {
            return this.position(x, y).size(width, height);
        }

        public Builder cornerRadius(float radius) {
            this.cornerRadius = radius;
            return this;
        }

        public Builder text(Text text) {
            this.text = text;
            return this;
        }

        public Builder style(WidgetStyle style) {
            this.style = style;
            return this;
        }

        public void render() {
            float finalCornerRadius = this.cornerRadius < 0 ? 0.5f * Math.min(this.width, this.height) : this.cornerRadius;

            context.state.addSpecialElement(new LiquidGlassGuiElementRenderState(this.x, this.y, this.x + this.width, this.y + this.height, finalCornerRadius, this.text, this.style));
        }
    }
}