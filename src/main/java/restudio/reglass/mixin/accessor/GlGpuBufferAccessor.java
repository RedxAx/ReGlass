package restudio.reglass.mixin.accessor;

import net.minecraft.client.gl.GlGpuBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GlGpuBuffer.class)
public interface GlGpuBufferAccessor {
    @Accessor("id")
    int getId();
}
