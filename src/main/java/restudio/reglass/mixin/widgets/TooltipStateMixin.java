package restudio.reglass.mixin.widgets;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.tooltip.TooltipState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.api.ReGlassConfig;

@Mixin(TooltipState.class)
public class TooltipStateMixin {

    @Shadow @Nullable private Tooltip tooltip;

//    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
//    private void onRender(DrawContext context, int mouseX, int mouseY, boolean hovered, boolean focused, ScreenRect navigationFocus, CallbackInfo ci) {
//        if (!ReGlassConfig.INSTANCE.features.enableRedesign) return;
//
//        ci.cancel();
//    }
}