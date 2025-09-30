package restudio.reglass.mixin.logical;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.gui.render.GuiRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.LiquidGlassUniforms;
import restudio.reglass.client.LiquidGlassPrecomputeRuntime;

@Mixin(GuiRenderer.class)
public class GuiRendererUniformsMixin {

    @Inject(method = "render(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V", at = @At("HEAD"))
    private void reglass$beforeGui(GpuBufferSlice fogBuffer, CallbackInfo ci) {
        LiquidGlassUniforms.get().beginFrame();
        LiquidGlassPrecomputeRuntime.get().run();
    }
}