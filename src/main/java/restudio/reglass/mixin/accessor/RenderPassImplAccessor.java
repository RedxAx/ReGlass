package restudio.reglass.mixin.accessor;

import net.minecraft.client.gl.CompiledShaderPipeline;
import net.minecraft.client.gl.RenderPassImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderPassImpl.class)
public interface RenderPassImplAccessor {
    @Accessor("pipeline")
    CompiledShaderPipeline reglass$getPipeline();
}