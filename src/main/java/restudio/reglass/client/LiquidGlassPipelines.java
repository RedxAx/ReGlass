package restudio.reglass.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public final class LiquidGlassPipelines {
    private static RenderPipeline LIQUID_GLASS_GUI;

    private LiquidGlassPipelines() {}

    public static synchronized RenderPipeline getGuiPipeline() {
        if (LIQUID_GLASS_GUI == null) {
            RenderPipeline.Builder b = RenderPipeline.builder()
                    .withLocation(Identifier.of("reglass", "pipeline/liquid_glass_gui"))
                    .withVertexShader(Identifier.of("reglass", "core/blit_fullscreen"))
                    .withFragmentShader(Identifier.of("reglass", "program/liquid_glass_gui"))
                    .withUniform("Projection", UniformType.UNIFORM_BUFFER)
                    .withUniform("SamplerInfo", UniformType.UNIFORM_BUFFER)
                    .withUniform("CustomUniforms", UniformType.UNIFORM_BUFFER)
                    .withUniform("WidgetInfo", UniformType.UNIFORM_BUFFER)
                    .withSampler("Sampler0")
                    .withSampler("Sampler1")
                    .withSampler("Sampler2")
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withDepthWrite(true)
                    .withVertexFormat(VertexFormats.POSITION, VertexFormat.DrawMode.QUADS);

            LIQUID_GLASS_GUI = b.build();
            RenderSystem.getDevice().precompilePipeline(LIQUID_GLASS_GUI, null);
        }
        return LIQUID_GLASS_GUI;
    }
}