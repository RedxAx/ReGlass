package restudio.reglass.mixin.logical;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.LiquidGlassPipelines;
import restudio.reglass.client.LiquidGlassPrecomputeRuntime;
import restudio.reglass.client.LiquidGlassUniforms;

import java.util.OptionalDouble;
import java.util.OptionalInt;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "render", at = @At("HEAD"))
    private void reglass$beginGuiFrame(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        LiquidGlassUniforms.get().beginFrame();
    }

    @Inject(method = "renderBlur", at = @At("HEAD"), cancellable = true)
    private void reglass$renderLiquidGlass(CallbackInfo ci) {
        LiquidGlassUniforms uniforms = LiquidGlassUniforms.get();
        if (uniforms.getCount() > 0) {
            ci.cancel();

            uniforms.uploadSharedUniforms();
            uniforms.uploadWidgetInfo();

            LiquidGlassPrecomputeRuntime.get().run();

            Framebuffer mainFb = this.client.getFramebuffer();

            try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                    () -> "reglass liquid glass pass",
                    mainFb.getColorAttachmentView(),
                    OptionalInt.empty(),
                    mainFb.useDepthAttachment ? mainFb.getDepthAttachmentView() : null,
                    OptionalDouble.empty()
            )) {
                RenderPipeline pipeline = LiquidGlassPipelines.getGuiPipeline();
                pass.setPipeline(pipeline);

                RenderSystem.bindDefaultUniforms(pass);
                pass.setUniform("SamplerInfo", uniforms.getSamplerInfoBuffer());
                pass.setUniform("CustomUniforms", uniforms.getCustomUniformsBuffer());
                pass.setUniform("WidgetInfo", uniforms.getWidgetInfoBuffer());
                pass.setUniform("BgConfig", uniforms.getBgConfigBuffer());

                pass.bindSampler("Sampler0", mainFb.getColorAttachmentView());
                pass.bindSampler("Sampler1", LiquidGlassPrecomputeRuntime.get().getBlurredView());

                GpuBuffer quadVB = RenderSystem.getQuadVertexBuffer();
                RenderSystem.ShapeIndexBuffer quadIBInfo = RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS);
                GpuBuffer quadIB = quadIBInfo.getIndexBuffer(6);

                pass.setVertexBuffer(0, quadVB);
                pass.setIndexBuffer(quadIB, quadIBInfo.getIndexType());
                pass.drawIndexed(0, 0, 6, 1);
            }
        }
    }
}