package restudio.reglass.client.api;

//? if >= 26 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
//? } else {
/*import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
*///? }
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
//? if >= 26 {
import restudio.reglass.client.LiquidGlassUniforms;
//? }
import restudio.reglass.client.gui.LiquidGlassGuiElementRenderState;
import restudio.reglass.mixin.accessor.DrawContextAccessor;
//? if >= 26 {
import restudio.reglass.mixin.accessor.ScissorStackAccessor;
//? }

public final class ReGlassApi {
    private ReGlassApi() {}
    public static WidgetStyle inactiveStyle = new WidgetStyle().tint(0x000000, 0.3f);

    public static ReGlassConfig getGlobalConfig() {
        return ReGlassConfig.INSTANCE;
    }

//? if >= 26 {
    public static Builder create(GuiGraphicsExtractor context) {
//? } else {
    /*public static Builder create(DrawContext context) {
*///? }
        return new Builder(context);
    }

    public static class Builder {
//? if >= 26 {
        private final GuiGraphicsExtractor context;
//? } else {
        /*private final DrawContext context;
*///? }
        private int x, y, width, height;
        private float cornerRadius = -1f;
//? if >= 26 {
        @Nullable private Component text = null;
//? } else {
        /*@Nullable private Text text = null;
*///? }
        private WidgetStyle style = new WidgetStyle();
        private float hoverAmount = 0f;
        private float focusAmount = 0f;
//? if >= 26 {
        private boolean screenSpace = false;
//? }

//? if >= 26 {
        private Builder(GuiGraphicsExtractor context) {
//? } else {
        /*private Builder(DrawContext context) {
*///? }
            this.context = context;
        }

//? if >= 26 {
        public Builder fromWidget(AbstractWidget widget) {
//? } else {
        /*public Builder fromWidget(ClickableWidget widget) {
*///? }
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

//? if >= 26 {
        public Builder text(Component text) {
//? } else {
        /*public Builder text(Text text) {
*///? }
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

//? if >= 26 {
        public Builder screenSpace() {
            this.screenSpace = true;
            return this;
        }

//? }
        public void render() {
            float finalCornerRadius = this.cornerRadius < 0 ? 0.5f * Math.min(this.width, this.height) : this.cornerRadius;
//? if >= 26 {
            Matrix3x2f pose = this.screenSpace ? new Matrix3x2f() : new Matrix3x2f(context.pose());
            ScreenRectangle scissorRect = ((ScissorStackAccessor) ((DrawContextAccessor) context).getScissorStack()).reglass$peek();
            LiquidGlassUniforms.get().tryApplyBlur(context);
            LiquidGlassGuiElementRenderState element = new LiquidGlassGuiElementRenderState(
//? } else {
            /*Matrix3x2f pose = new Matrix3x2f(context.getMatrices());
            ScreenRect scissorRect = ((DrawContextAccessor) context).getScissorStack().peekLast();
            context.state.addSpecialElement(new LiquidGlassGuiElementRenderState(
*///? }
                    this.x, this.y, this.x + this.width, this.y + this.height,
                    finalCornerRadius, this.text, this.style, pose, scissorRect,
                    this.hoverAmount, this.focusAmount
//? if >= 26 {
            );
            LiquidGlassUniforms.get().addWidget(element);
            ((DrawContextAccessor) context).getGuiRenderState().addGuiElement(element);
//? } else {
            /*));
*///? }
        }
    }
}
