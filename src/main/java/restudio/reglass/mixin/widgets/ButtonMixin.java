package restudio.reglass.mixin.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.Config;
import restudio.reglass.client.LiquidGlassOverlay;


@Mixin(PressableWidget.class)
public class ButtonMixin {

    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
    private void onRender(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (Config.redesginMinecraft) {
            PressableWidget widget = (PressableWidget) (Object) this;
            if (widget instanceof TextIconButtonWidget) return;
            LiquidGlassOverlay.get().registerWidgetRect(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight(), 0.5f * Math.min(widget.getWidth(), widget.getHeight()));
            context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, widget.getMessage(), widget.getX() + widget.getWidth() / 2, widget.getY() + (widget.getHeight() - 8) / 2, 0xFFFFFFFF);
            ci.cancel();
        }
    }
}