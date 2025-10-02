package restudio.reglass.mixin.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.Config;
import restudio.reglass.client.LiquidGlassUniforms;
import restudio.reglass.client.api.ReGlassApi;

@Mixin(SliderWidget.class)
public abstract class SliderWidgetMixin extends ClickableWidget {

    public SliderWidgetMixin(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
    private void onRenderWidget(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (Config.redesginMinecraft) {
            ci.cancel();

            ReGlassApi.create(context).fromWidget(this).render();

            LiquidGlassUniforms.get().tryApplyBlur(context);

            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            int color = this.active ? 0xFFFFFF : 0xA0A0A0;
            int finalColor = color | MathHelper.ceil(this.alpha * 255.0f) << 24;

            context.drawCenteredTextWithShadow(textRenderer, this.getMessage(), this.getX() + this.getWidth() / 2, this.getY() + (this.getHeight() - 8) / 2, finalColor);
        }
    }
}