package restudio.demos.liquidglass.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.gui.render.GuiRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.demos.liquidglass.client.LiquidGlassOverlay;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin {
    @Inject(method = "render(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V", at = @At("TAIL"))
    private void liquidglass$afterGui(GpuBufferSlice fogBuffer, CallbackInfo ci) {
        LiquidGlassOverlay.get().renderAfterGui();
    }
}