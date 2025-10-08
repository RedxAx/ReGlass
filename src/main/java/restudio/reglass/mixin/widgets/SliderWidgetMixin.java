package restudio.reglass.mixin.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
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

@Mixin(SliderWidget.class)
public abstract class SliderWidgetMixin extends ClickableWidget {

    @Unique
    WidgetStyle knobStyle =
            new WidgetStyle().smoothing(-0.005f).tint(0x000000, 0.1f);

    public SliderWidgetMixin(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
    private void onRenderWidget(
            DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci
    ) {
        if (!ReGlassConfig.INSTANCE.features.enableRedesign
                || !ReGlassConfig.INSTANCE.features.sliders) {
            return;
        }

        ci.cancel();

        int knobX = (int) (this.getX()
                + (((SliderWidgetAccessor) this).getValue()
                * (this.getWidth() - 4)));

        ReGlassApi.create(context).fromWidget(this).render();
        ReGlassApi.create(context)
                .size(4, getHeight())
                .position(knobX, getY())
                .style(knobStyle)
                .render();

        LiquidGlassUniforms.get().tryApplyBlur(context);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int color = this.active ? 0xFFFFFF : 0xA0A0A0;
        int finalColor = color | MathHelper.ceil(this.alpha * 255.0f) << 24;

        context.drawCenteredTextWithShadow(
                textRenderer,
                this.getMessage(),
                this.getX() + this.getWidth() / 2,
                this.getY() + (this.getHeight() - 8) / 2,
                finalColor
        );
    }
}