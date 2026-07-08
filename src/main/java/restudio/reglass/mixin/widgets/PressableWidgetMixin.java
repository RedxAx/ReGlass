package restudio.reglass.mixin.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.util.math.ColorHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.LiquidGlassUniforms;
import restudio.reglass.client.api.ReGlassApi;
import restudio.reglass.client.api.ReGlassConfig;
import restudio.reglass.client.api.WidgetStyle;
import restudio.reglass.extensions.PressableWidgetExtension;

@Mixin(PressableWidget.class)
public abstract class PressableWidgetMixin implements PressableWidgetExtension {
    @Unique
    private boolean reglass$isDragging;
    @Shadow
    public abstract void drawMessage(DrawContext context, TextRenderer textRenderer, int color);

    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
    private void modifyRenderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if ((Object) this instanceof PressableWidget pressableWidget) {
            if (!ReGlassConfig.INSTANCE.features.enableRedesign || !ReGlassConfig.INSTANCE.features.buttons) {
                return;
            }

            boolean isDisabled = !pressableWidget.active;
            boolean isHighlighted = pressableWidget.isHovered();
            boolean isFocused = isHighlighted && reglass$isDragging;

            if (ReGlassConfig.INSTANCE.features.enableRedesign && ReGlassConfig.INSTANCE.features.buttons) {
                ReGlassApi.create(context)
                        .position(pressableWidget.getX(), pressableWidget.getY())
                        .size(pressableWidget.getWidth(), pressableWidget.getHeight())
                        .hover(isHighlighted ? 1f : 0f)
                        .focus(isFocused ? 1.0f : 0.0f)
                        .style(WidgetStyle.create().tint(isDisabled ? 0xFF000000 : 0xFFFFFFFF, isDisabled ? 0.4f : 0f))
                        .render();

                LiquidGlassUniforms.get().tryApplyBlur(context);

                TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
                int i = ColorHelper.withAlpha(1.0f, pressableWidget.active ? -1 : -6250336);
                drawMessage(context, textRenderer, i);
                // context.drawCenteredTextWithShadow(textRenderer, buttonWidget.getMessage(), buttonWidget.getX() + buttonWidget.getWidth() / 2, buttonWidget.getY() + (buttonWidget.getHeight() - 8) / 2, finalColor);

                ci.cancel();
            }
        }
    }

    @Override
    public void reglass$setIsDragging(boolean isDragging) {
        this.reglass$isDragging = isDragging;
    }

    @Override
    public boolean reglass$isDragging() {
        return reglass$isDragging;
    }
}
