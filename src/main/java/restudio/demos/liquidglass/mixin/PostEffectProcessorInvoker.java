// File: restudio/demos/liquidglass/mixin/PostEffectProcessorInvoker.java
package restudio.demos.liquidglass.mixin;

import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gl.PostEffectPipeline;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.ProjectionMatrix2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Set;

@Mixin(PostEffectProcessor.class)
public interface PostEffectProcessorInvoker {
    @Invoker("parseEffect")
    static PostEffectProcessor invokeParseEffect(PostEffectPipeline pipeline, TextureManager textureManager, Set<Identifier> set, Identifier id, ProjectionMatrix2 projectionMatrix) {
        throw new AssertionError();
    }
}
