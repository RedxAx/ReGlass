package restudio.reglass.mixin.logical;

import java.util.Collection;
import net.minecraft.client.gl.GlCommandEncoder;
import net.minecraft.client.gl.RenderPassImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import restudio.reglass.client.LiquidGlassUniforms;

@Mixin(GlCommandEncoder.class)
public class GlCommandEncoderMixin {

    @Inject(method = "setupRenderPass", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_glUseProgram(I)V", shift = At.Shift.AFTER))
    private void reglass$attachUboToPass(RenderPassImpl pass, Collection<String> skippedUniforms, CallbackInfoReturnable<Boolean> cir) {
        var u = LiquidGlassUniforms.get();
        pass.setUniform("SamplerInfo",   u.getSamplerInfoBuffer());
        pass.setUniform("CustomUniforms",u.getCustomUniformsBuffer());
        pass.setUniform("WidgetInfo",    u.getWidgetInfoBuffer());
    }
}