package restudio.demos.liquidglass.mixin;

import net.minecraft.client.gl.ShaderLoader;
import net.minecraft.client.render.ProjectionMatrix2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShaderLoader.class)
public interface ShaderLoaderAccessor {
    @Accessor("projectionMatrix")
    ProjectionMatrix2 getProjectionMatrix();
}
