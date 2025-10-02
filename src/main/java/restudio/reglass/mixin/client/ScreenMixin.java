package restudio.reglass.mixin.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.LiquidGlassUniforms;

@Mixin(Screen.class)
public class ScreenMixin {

    @Inject(
            method = "renderBackground(Lnet/minecraft/client/gui/DrawContext;IIF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;applyBlur(Lnet/minecraft/client/gui/DrawContext;)V")
    )
    private void reglass$onScreenBlur(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        LiquidGlassUniforms.get().setScreenWantsBlur(true);
    }
}