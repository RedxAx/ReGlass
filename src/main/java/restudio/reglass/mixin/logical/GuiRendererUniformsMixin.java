package restudio.reglass.mixin.logical;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.gui.render.GuiRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderer.class)
public class GuiRendererUniformsMixin {

    @Inject(method = "renderPreparedDraws", at = @At("HEAD"))
    private void reglass$uploadWidgetsBeforeDraw(GpuBufferSlice dynamic, CallbackInfo ci) {
    }
}