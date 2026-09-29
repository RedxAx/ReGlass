package restudio.reglass.client.api;

//#if MC >= 26
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
//#else
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
//#endif
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
//#if MC >= 26
import restudio.reglass.client.LiquidGlassUniforms;
//#endif
import restudio.reglass.client.gui.LiquidGlassGuiElementRenderState;
import restudio.reglass.mixin.accessor.DrawContextAccessor;
//#if MC >= 26
import restudio.reglass.mixin.accessor.ScissorStackAccessor;
//#endif

public final class ReGlassApi {
    private ReGlassApi() {}
    public static WidgetStyle inactiveStyle = new WidgetStyle().tint(0x000000, 0.3f);

    public static ReGlassConfig getGlobalConfig() {
        return ReGlassConfig.INSTANCE;
    }

//#if MC >= 26
    public static Builder create(GuiGraphicsExtractor context) {
//#else
    public static Builder create(DrawContext context) {
//#endif
        return new Builder(context);
    }

    public static class Builder {
//#if MC >= 26
        private final GuiGraphicsExtractor context;
//#else
        private final DrawContext context;
//#endif
        private int x, y, width, height;
        private float cornerRadius = -1f;
//#if MC >= 26
        @Nullable private Component text = null;
//#else
        @Nullable private Text text = null;
//#endif
        private WidgetStyle style = new WidgetStyle();
        private float hoverAmount = 0f;
        private float focusAmount = 0f;
//#if MC >= 26
        private boolean screenSpace = false;
//#endif

//#if MC >= 26
        private Builder(GuiGraphicsExtractor context) {
//#else
        private Builder(DrawContext context) {
//#endif
            this.context = context;
        }

//#if MC >= 26
        public Builder fromWidget(AbstractWidget widget) {
//#else
        public Builder fromWidget(ClickableWidget widget) {
//#endif
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

//#if MC >= 26
        public Builder text(Component text) {
//#else
        public Builder text(Text text) {
//#endif
            this.text = text;
            return this;
        }

        public Builder style(WidgetStyle style) {
            this.style = style;
            return this;
        }

        public Builder hover(float amount) {
            if (Float.isNaN(amount)) amount = 0f;
            this.hoverAmount = Math.max(0f, Math.min(1f, amount));
            return this;
        }

        public Builder focus(float amount) {
            if (Float.isNaN(amount)) amount = 0f;
            this.focusAmount = Math.max(0f, Math.min(1f, amount));
            return this;
        }

        public Builder selected(float amount) {
            return this.focus(amount);
        }

//#if MC >= 26
        public Builder screenSpace() {
            this.screenSpace = true;
            return this;
        }

//#endif
        public void render() {
            float finalCornerRadius = this.cornerRadius < 0 ? 0.5f * Math.min(this.width, this.height) : this.cornerRadius;
//#if MC >= 26
            Matrix3x2f pose = this.screenSpace ? new Matrix3x2f() : new Matrix3x2f(context.pose());
            ScreenRectangle scissorRect = ((ScissorStackAccessor) ((DrawContextAccessor) context).getScissorStack()).reglass$peek();
            LiquidGlassUniforms.get().tryApplyBlur(context);
            LiquidGlassGuiElementRenderState element = new LiquidGlassGuiElementRenderState(
//#else
            Matrix3x2f pose = new Matrix3x2f(context.getMatrices());
            ScreenRect scissorRect = ((DrawContextAccessor) context).getScissorStack().peekLast();
            context.state.addSpecialElement(new LiquidGlassGuiElementRenderState(
//#endif
                    this.x, this.y, this.x + this.width, this.y + this.height,
                    finalCornerRadius, this.text, this.style, pose, scissorRect,
                    this.hoverAmount, this.focusAmount
//#if MC >= 26
            );
            LiquidGlassUniforms.get().addWidget(element);
            ((DrawContextAccessor) context).getGuiRenderState().addGuiElement(element);
//#else
            ));
//#endif
        }
    }
}
