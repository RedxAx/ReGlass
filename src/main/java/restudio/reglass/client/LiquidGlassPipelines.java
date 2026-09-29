package restudio.reglass.client;

//#if MC >= 26.3
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
//#else
import com.mojang.blaze3d.pipeline.RenderPipeline;
//#endif
//#if MC >= 26.2
//#if MC >= 26.3
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
//#else
import com.mojang.blaze3d.PrimitiveTopology;
//#endif
//#if MC >= 26.3
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
//#else
import com.mojang.blaze3d.pipeline.BindGroupLayout;
//#endif
//#if MC >= 26.3
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
//#else
import com.mojang.blaze3d.pipeline.ColorTargetState;
//#endif
//#elseif MC >= 26
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.platform.CompareOp;
//#else
import com.mojang.blaze3d.platform.DepthTestFunction;
//#endif
import com.mojang.blaze3d.systems.RenderSystem;
//#if MC >= 26.3
import com.mojang.renderpearl.api.vertex.VertexFormat;
//#else
import com.mojang.blaze3d.vertex.VertexFormat;
//#endif
//#if MC >= 26
//#if MC >= 26.3
import com.mojang.renderpearl.api.pipeline.UniformType;
//#else
import com.mojang.blaze3d.shaders.UniformType;
//#endif
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.resources.Identifier;
//#else
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
//#endif

public final class LiquidGlassPipelines {
    private static RenderPipeline LIQUID_GLASS_GUI;

    private LiquidGlassPipelines() {}

    public static synchronized RenderPipeline getGuiPipeline() {
        if (LIQUID_GLASS_GUI == null) {
            RenderPipeline.Builder b = RenderPipeline.builder()
//#if MC >= 26
                    .withLocation(Identifier.fromNamespaceAndPath("reglass", "pipeline/liquid_glass_gui"))
                    .withVertexShader(Identifier.fromNamespaceAndPath("reglass", "core/blit_fullscreen"))
                    .withFragmentShader(Identifier.fromNamespaceAndPath("reglass", "program/liquid_glass_gui"))
//#else
                    .withLocation(Identifier.of("reglass", "pipeline/liquid_glass_gui"))
                    .withVertexShader(Identifier.of("reglass", "core/blit_fullscreen"))
                    .withFragmentShader(Identifier.of("reglass", "program/liquid_glass_gui"))
//#endif
//#if MC >= 26.2
                    //Vulkan requires pipeline layouts to match actual shader bindings
                    //unused entries can break pipeline creation
                    .withBindGroupLayout(
                            BindGroupLayout.builder()
                                    .withUniform("SamplerInfo", UniformType.UNIFORM_BUFFER)
                                    .withUniform("CustomUniforms", UniformType.UNIFORM_BUFFER)
                                    .withUniform("WidgetInfo", UniformType.UNIFORM_BUFFER)
                                    .withUniform("BgConfig", UniformType.UNIFORM_BUFFER)
//#if MC >= 26.3
                                    .withUniform("Sampler0", UniformType.COMBINED_IMAGE_SAMPLER)
//#else
                                    .withSampler("Sampler0")
//#endif
//#if MC >= 26.3
                                    .withUniform("Sampler1", UniformType.COMBINED_IMAGE_SAMPLER)
//#else
                                    .withSampler("Sampler1")
//#endif
//#if MC >= 26.3
                                    .withUniform("Sampler2", UniformType.COMBINED_IMAGE_SAMPLER)
//#else
                                    .withSampler("Sampler2")
//#endif
//#if MC >= 26.3
                                    .withUniform("Sampler3", UniformType.COMBINED_IMAGE_SAMPLER)
//#else
                                    .withSampler("Sampler3")
//#endif
//#if MC >= 26.3
                                    .withUniform("Sampler4", UniformType.COMBINED_IMAGE_SAMPLER)
//#else
                                    .withSampler("Sampler4")
//#endif
//#if MC >= 26.3
                                    .withUniform("Sampler5", UniformType.COMBINED_IMAGE_SAMPLER)
//#else
                                    .withSampler("Sampler5")
//#endif
                                    .build()
                    )
                    .withVertexBinding(0, DefaultVertexFormat.POSITION)
                    .withPrimitiveTopology(PrimitiveTopology.QUADS)
                    //Minecraft leaves the no-depth Vulkan pipeline variant invalid, which can crash when bound.
                    //the blend attachment still has to match the color target format
                    .withColorTargetState(ColorTargetState.DEFAULT);
//#else
                    .withUniform("Projection", UniformType.UNIFORM_BUFFER)
                    .withUniform("SamplerInfo", UniformType.UNIFORM_BUFFER)
                    .withUniform("CustomUniforms", UniformType.UNIFORM_BUFFER)
                    .withUniform("WidgetInfo", UniformType.UNIFORM_BUFFER)
                    .withUniform("BgConfig", UniformType.UNIFORM_BUFFER)
                    .withSampler("Sampler0")
                    .withSampler("Sampler1")
                    .withSampler("Sampler2")
                    .withSampler("Sampler3")
                    .withSampler("Sampler4")
                    .withSampler("Sampler5")
//#if MC >= 26
                    .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
                    .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS);
//#else
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withDepthWrite(false)
                    .withVertexFormat(VertexFormats.POSITION, VertexFormat.DrawMode.QUADS);
//#endif
//#endif

            LIQUID_GLASS_GUI = b.build();
//#if MC >= 26.2
//#if MC >= 26.3
            RenderSystem.getCompiledPipeline(LIQUID_GLASS_GUI);
//#else
            RenderSystem.getDevice().precompilePipeline(LIQUID_GLASS_GUI);
//#endif
//#else
            RenderSystem.getDevice().precompilePipeline(LIQUID_GLASS_GUI, null);
//#endif
        }
        return LIQUID_GLASS_GUI;
    }
}
