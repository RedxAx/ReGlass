package restudio.reglass.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
//? if >= 26.2 {
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.ColorTargetState;
//? } elif >= 26 {
/*import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.platform.CompareOp;
*///? } else {
/*import com.mojang.blaze3d.platform.DepthTestFunction;
*///? }
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
//? if >= 26 {
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.resources.Identifier;
//? } else {
/*import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
*///? }

public final class LiquidGlassPipelines {
    private static RenderPipeline LIQUID_GLASS_GUI;

    private LiquidGlassPipelines() {}

    public static synchronized RenderPipeline getGuiPipeline() {
        if (LIQUID_GLASS_GUI == null) {
            RenderPipeline.Builder b = RenderPipeline.builder()
//? if >= 26 {
                    .withLocation(Identifier.fromNamespaceAndPath("reglass", "pipeline/liquid_glass_gui"))
                    .withVertexShader(Identifier.fromNamespaceAndPath("reglass", "core/blit_fullscreen"))
                    .withFragmentShader(Identifier.fromNamespaceAndPath("reglass", "program/liquid_glass_gui"))
//? } else {
                    /*.withLocation(Identifier.of("reglass", "pipeline/liquid_glass_gui"))
                    .withVertexShader(Identifier.of("reglass", "core/blit_fullscreen"))
                    .withFragmentShader(Identifier.of("reglass", "program/liquid_glass_gui"))
*///? }
//? if >= 26.2 {
                    //Vulkan requires pipeline layouts to match actual shader bindings
                    //unused entries can break pipeline creation
                    .withBindGroupLayout(
                            BindGroupLayout.builder()
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
                                    .build()
                    )
                    .withVertexBinding(0, DefaultVertexFormat.POSITION)
                    .withPrimitiveTopology(PrimitiveTopology.QUADS)
                    //Minecraft leaves the no-depth Vulkan pipeline variant invalid, which can crash when bound.
                    //the blend attachment still has to match the color target format
                    .withColorTargetState(ColorTargetState.DEFAULT);
//? } else {
                    /*.withUniform("Projection", UniformType.UNIFORM_BUFFER)
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
//? if >= 26 {
                    .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
                    .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS);
//? } else {
                    /^.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withDepthWrite(false)
                    .withVertexFormat(VertexFormats.POSITION, VertexFormat.DrawMode.QUADS);
^///? }
*///? }

            LIQUID_GLASS_GUI = b.build();
//? if >= 26.2 {
            RenderSystem.getDevice().precompilePipeline(LIQUID_GLASS_GUI);
//? } else {
            /*RenderSystem.getDevice().precompilePipeline(LIQUID_GLASS_GUI, null);
*///? }
        }
        return LIQUID_GLASS_GUI;
    }
}
