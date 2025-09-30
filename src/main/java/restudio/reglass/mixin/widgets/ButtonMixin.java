package restudio.reglass.mixin.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import org.joml.Matrix3x2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.Config;
import restudio.reglass.client.LiquidGlassGui;

@Mixin(PressableWidget.class)
public class ButtonMixin {

    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
    private void onRender(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (Config.redesginMinecraft) {
            PressableWidget widget = (PressableWidget) (Object) this;
            if (widget instanceof TextIconButtonWidget) return;
            GuiRenderState state = context.state;
            Matrix3x2f pose = new Matrix3x2f().identity();
            LiquidGlassGui.get().addBlob(state, pose, widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight());
            context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, widget.getMessage(), widget.getX() + widget.getWidth() / 2, widget.getY() + (widget.getHeight() - 8) / 2, 0xFFFFFFFF);
            ci.cancel();
        }
    }
}