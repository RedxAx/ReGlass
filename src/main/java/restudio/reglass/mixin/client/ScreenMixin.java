package restudio.reglass.mixin.client;

//#if MC >= 26
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
//#else
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.screen.Screen;
//#endif
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.LiquidGlassUniforms;
import restudio.reglass.client.api.ReGlassConfig;
//#if MC < 26
import restudio.reglass.mixin.accessor.GuiRenderStateAccessor;
//#endif

@Mixin(Screen.class)
public class ScreenMixin {

//#if MC >= 26
    @Inject(
            method = "extractBlurredBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void reglass$skipDuplicateBlur(GuiGraphicsExtractor context, CallbackInfo ci) {
        if (LiquidGlassUniforms.get().screenWantsBlur()) {
//#else
    @Inject(method = "renderBackground(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;applyBlur(Lnet/minecraft/client/gui/DrawContext;)V"))
    private void reglass$onScreenBlur(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        LiquidGlassUniforms.get().setScreenWantsBlur(true);
    }

    @Inject(method = "applyBlur", at = @At("HEAD"), cancellable = true)
    private void reglass$checkBeforeBlur(DrawContext context, CallbackInfo ci) {
        GuiRenderState state = context.state;
        int blurLayer = ((GuiRenderStateAccessor) state).getBlurLayer();
        if (blurLayer != Integer.MAX_VALUE) {
//#endif
            ci.cancel();
        }
    }

//#if MC < 26
    @Inject(method = "renderInGameBackground", at = @At("HEAD"), cancellable = true)
    private void reglass$onRenderInGameBackground(DrawContext context, CallbackInfo ci) {
        if (ReGlassConfig.INSTANCE.features.enableRedesign && ReGlassConfig.INSTANCE.features.cancelScreenDarkening) {
            ci.cancel();
        }
    }
//#endif

    @Inject(
//#if MC >= 26
            method = "extractTransparentBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V",
//#else
            method = "renderDarkening(Lnet/minecraft/client/gui/DrawContext;)V",
//#endif
            at = @At("HEAD"),
            cancellable = true
    )
//#if MC >= 26
    private void reglass$onRenderDarkening(GuiGraphicsExtractor context, CallbackInfo ci) {
//#else
    private void reglass$onRenderDarkening(DrawContext context, CallbackInfo ci) {
//#endif
        if (ReGlassConfig.INSTANCE.features.enableRedesign && ReGlassConfig.INSTANCE.features.cancelScreenDarkening) {
            ci.cancel();
        }
    }
}
