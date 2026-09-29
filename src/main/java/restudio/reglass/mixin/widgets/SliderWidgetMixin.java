package restudio.reglass.mixin.widgets;

//#if MC >= 26
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
//#else
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
//#endif
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.LiquidGlassUniforms;
import restudio.reglass.client.api.ReGlassApi;
import restudio.reglass.client.api.ReGlassConfig;
import restudio.reglass.client.api.WidgetStyle;
import restudio.reglass.mixin.accessor.SliderWidgetAccessor;

//#if MC >= 26
@Mixin(AbstractSliderButton.class)
public abstract class SliderWidgetMixin extends AbstractWidget {
//#else
@Mixin(SliderWidget.class)
public abstract class SliderWidgetMixin extends ClickableWidget {
//#endif

    @Unique
    WidgetStyle knobStyle = new WidgetStyle().smoothing(-0.005f).tint(0x000000, 0.1f);
    @Unique
    private double reglass$knobX = Double.NaN;

//#if MC >= 26
    public SliderWidgetMixin(int x, int y, int width, int height, Component message) {
//#else
    public SliderWidgetMixin(int x, int y, int width, int height, Text message) {
//#endif
        super(x, y, width, height, message);
    }

//#if MC >= 26
    @Inject(method = "extractWidgetRenderState", at = @At("HEAD"), cancellable = true)
    private void onRenderWidget(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
//#else
    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
    private void onRenderWidget(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
//#endif
        if (!ReGlassConfig.INSTANCE.features.enableRedesign || !ReGlassConfig.INSTANCE.features.sliders) {
            return;
        }

        ci.cancel();

        double targetKnobX = this.getX() + (((SliderWidgetAccessor) this).getValue() * (this.getWidth() - 4));
        if (Double.isNaN(this.reglass$knobX)) {
            this.reglass$knobX = targetKnobX;
        }
//#if MC >= 26
        double deltaTicks = Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks();
        double deltaSeconds = deltaTicks / 20.0;
//#else
        double deltaSeconds = delta / 20.0;
//#endif
        double alpha = 1.0 - Math.exp(-deltaSeconds / 0.08);
        if (alpha < 0.0) alpha = 0.0;
        if (alpha > 1.0) alpha = 1.0;
        this.reglass$knobX += (targetKnobX - this.reglass$knobX) * alpha;

        int knobX = (int)Math.round(this.reglass$knobX);
        boolean hoveredBg = this.isMouseOver(mouseX, mouseY);
        boolean knobHovered = mouseX >= knobX && mouseX < knobX + 4 && mouseY >= getY() && mouseY < getY() + getHeight();
        boolean focus = this.isFocused();

        ReGlassApi.create(context).fromWidget(this).hover(hoveredBg ? 1f : 0f).focus(focus ? 1f : 0f).render();
        ReGlassApi.create(context)
                .size(4, getHeight())
                .position(knobX, getY())
                .style(knobStyle)
                .hover(knobHovered ? 1f : 0f)
                .focus(focus ? 1f : 0f)
                .render();

        LiquidGlassUniforms.get().tryApplyBlur(context);

//#if MC >= 26
        Font font = Minecraft.getInstance().font;
//#else
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
//#endif
        int color = this.active ? 0xFFFFFF : 0xA0A0A0;
//#if MC >= 26
        int finalColor = color | Mth.ceil(this.alpha * 255.0f) << 24;
//#else
        int finalColor = color | MathHelper.ceil(this.alpha * 255.0f) << 24;
//#endif

//#if MC >= 26
        context.centeredText(font, this.getMessage(), this.getX() + this.getWidth() / 2, this.getY() + (this.getHeight() - 8) / 2, finalColor);
//#else
        context.drawCenteredTextWithShadow(textRenderer, this.getMessage(), this.getX() + this.getWidth() / 2, this.getY() + (this.getHeight() - 8) / 2, finalColor);
//#endif
    }
}

