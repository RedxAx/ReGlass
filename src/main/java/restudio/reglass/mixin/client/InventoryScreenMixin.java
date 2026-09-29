package restudio.reglass.mixin.client;

//#if MC >= 26
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
//#else
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
//#endif
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import restudio.reglass.client.api.ReGlassConfig;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {
//#if MC >= 26
    @Inject(method = "extractLabels", at = @At("HEAD"), cancellable = true)
    private void reglass$hidePlayerInventoryLabels(GuiGraphicsExtractor context, int mouseX, int mouseY, CallbackInfo ci) {
//#else
    @Inject(method = "drawForeground", at = @At("HEAD"), cancellable = true)
    private void reglass$hidePlayerInventoryLabels(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
//#endif
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (cfg.features.enableRedesign && cfg.features.containers) {
            ci.cancel();
        }
    }

//#if MC >= 26
    @Inject(method = "getRecipeBookButtonPosition", at = @At("RETURN"), cancellable = true)
    private void reglass$moveRecipeBookButton(CallbackInfoReturnable<ScreenPosition> cir) {
//#else
    @Inject(method = "getRecipeBookButtonPos", at = @At("RETURN"), cancellable = true)
    private void reglass$moveRecipeBookButton(CallbackInfoReturnable<ScreenPos> cir) {
//#endif
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            return;
        }

//#if MC >= 26
        ScreenPosition original = cir.getReturnValue();
        cir.setReturnValue(new ScreenPosition(original.x() + 28, original.y() - 4));
//#else
        ScreenPos original = cir.getReturnValue();
        cir.setReturnValue(new ScreenPos(original.x() + 28, original.y() - 4));
//#endif
    }
}

