package restudio.reglass.mixin.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import restudio.reglass.client.Config;
import restudio.reglass.client.LiquidGlassUniforms;
import restudio.reglass.client.api.ReGlassConfig;
import restudio.reglass.mixin.accessor.GuiRenderStateAccessor;

@Mixin(Screen.class)
public class ScreenMixin {

    @Inject(method = "renderBackground(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;applyBlur(Lnet/minecraft/client/gui/DrawContext;)V"))
    private void reglass$onScreenBlur(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        LiquidGlassUniforms.get().setScreenWantsBlur(true);
    }

    @Inject(method = "applyBlur", at = @At("HEAD"), cancellable = true)
    private void reglass$checkBeforeBlur(DrawContext context, CallbackInfo ci) {
        GuiRenderState state = context.state;
        int blurLayer = ((GuiRenderStateAccessor) state).getBlurLayer();
        if (blurLayer != Integer.MAX_VALUE) {
            ci.cancel();
        }
    }

    @Inject(
            method = "keyPressed",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onKeyPressed(
            KeyInput input, CallbackInfoReturnable<Boolean> cir
    ) {
        if (input.key() == GLFW.GLFW_KEY_V) {
            Config.redesginMinecraft = !Config.redesginMinecraft;
            ReGlassConfig.INSTANCE.features.enableRedesign = Config.redesginMinecraft;
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = "renderDarkening(Lnet/minecraft/client/gui/DrawContext;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void reglass$onRenderDarkening(DrawContext context, CallbackInfo ci) {
        if (ReGlassConfig.INSTANCE.features.enableRedesign && ReGlassConfig.INSTANCE.features.cancelScreenDarkening) {
            ci.cancel();
        }
    }
}