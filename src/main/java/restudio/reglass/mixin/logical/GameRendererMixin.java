package restudio.reglass.mixin.logical;

import com.mojang.blaze3d.buffers.GpuBuffer;
//#if MC >= 26
import com.mojang.blaze3d.pipeline.RenderTarget;
//#endif
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
//#if MC >= 26.2
import com.mojang.blaze3d.PrimitiveTopology;
//#endif
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.List;
import java.util.OptionalDouble;
//#if MC >= 26.2
import java.util.Optional;
//#else
import java.util.OptionalInt;
//#endif
//#if MC >= 26
import net.minecraft.client.Minecraft;
//#else
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
//#endif
import net.minecraft.client.gui.render.GuiRenderer;
//#if MC >= 26
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.DeltaTracker;
//#else
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
//#endif
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.LiquidGlassPipelines;
import restudio.reglass.client.LiquidGlassPrecomputeRuntime;
import restudio.reglass.client.LiquidGlassUniforms;
import restudio.reglass.client.api.ReGlassConfig;
import restudio.reglass.client.gui.QuadVertexBufferProvider;
import restudio.reglass.client.runtime.ReGlassAnim;
import restudio.reglass.mixin.accessor.GameRendererAccessor;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
//#if MC >= 26
    @Shadow @Final private Minecraft minecraft;
//#else
    @Shadow @Final private MinecraftClient client;
//#endif

//#if MC >= 26
    @Inject(method = "extract", at = @At("HEAD"))
    private void reglass$beginGuiFrame(DeltaTracker tickCounter, boolean tick, CallbackInfo ci) {
//#else
    @Inject(method = "render", at = @At("HEAD"))
    private void reglass$beginGuiFrame(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
//#endif
        double deltaTicks;
        try {
//#if MC >= 26
            deltaTicks = tickCounter.getRealtimeDeltaTicks();
//#else
            deltaTicks = tickCounter.getDynamicDeltaTicks();
//#endif
        } catch (Throwable t) {
            deltaTicks = 1.0 / 60.0 * 20.0;
        }
        double dt = deltaTicks / 20.0;
        LiquidGlassUniforms.get().beginFrame(dt);
        ReGlassAnim.INSTANCE.update(ReGlassConfig.INSTANCE, dt);
    }

//#if MC >= 26
    @Inject(method = "processBlurEffect", at = @At("HEAD"), cancellable = true)
//#else
    @Inject(method = "renderBlur", at = @At("HEAD"), cancellable = true)
//#endif
    private void reglass$renderLiquidGlass(CallbackInfo ci) {
        LiquidGlassUniforms uniforms = LiquidGlassUniforms.get();
        if (uniforms.getCount() > 0) {
            ci.cancel();
            uniforms.uploadSharedUniforms();
            uniforms.uploadWidgetInfo();
            List<Integer> radii = uniforms.getUsedBlurRadiiOrdered();
            LiquidGlassPrecomputeRuntime.get().setRequestedRadii(radii);
            LiquidGlassPrecomputeRuntime.get().run();
//#if MC >= 26.2
            RenderTarget mainFb = this.minecraft.gameRenderer.mainRenderTarget();
//#elseif MC >= 26
            RenderTarget mainFb = this.minecraft.getMainRenderTarget();
//#else
            Framebuffer mainFb = this.client.getFramebuffer();
//#endif
            try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                    () -> "reglass liquid glass pass",
//#if MC >= 26
                    mainFb.getColorTextureView(),
//#else
                    mainFb.getColorAttachmentView(),
//#endif
//#if MC >= 26.2
                    Optional.empty(),
//#else
                    OptionalInt.empty(),
//#endif
//#if MC >= 26
                    mainFb.useDepth ? mainFb.getDepthTextureView() : null,
//#else
                    mainFb.useDepthAttachment ? mainFb.getDepthAttachmentView() : null,
//#endif
                    OptionalDouble.empty()
            )) {
                RenderPipeline pipeline = LiquidGlassPipelines.getGuiPipeline();
                pass.setPipeline(pipeline);

//#if MC < 26.2
                RenderSystem.bindDefaultUniforms(pass);
//#endif
                pass.setUniform("SamplerInfo", uniforms.getSamplerInfoBuffer());
                pass.setUniform("CustomUniforms", uniforms.getCustomUniformsBuffer());
                pass.setUniform("WidgetInfo", uniforms.getWidgetInfoBuffer());
                pass.setUniform("BgConfig", uniforms.getBgConfigBuffer());
//#if MC >= 26
                pass.bindTexture("Sampler0", mainFb.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
//#else
                pass.bindTexture("Sampler0", mainFb.getColorAttachmentView(), RenderSystem.getSamplerCache().get(FilterMode.LINEAR));
//#endif

                GuiRenderer guiRenderer = ((GameRendererAccessor) this).getGuiRenderer();
                GpuBuffer quadVB = ((QuadVertexBufferProvider) guiRenderer).getQuadVertexBuffer();
//#if MC >= 26.2
                RenderSystem.AutoStorageIndexBuffer quadIBInfo = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
                com.mojang.blaze3d.buffers.GpuBuffer quadIB = quadIBInfo.getBuffer(6);
//#elseif MC >= 26
                RenderSystem.AutoStorageIndexBuffer quadIBInfo = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
                com.mojang.blaze3d.buffers.GpuBuffer quadIB = quadIBInfo.getBuffer(6);
//#else
                RenderSystem.ShapeIndexBuffer quadIBInfo = RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS);
                com.mojang.blaze3d.buffers.GpuBuffer quadIB = quadIBInfo.getIndexBuffer(6);
//#endif
//#if MC >= 26.2
                pass.setVertexBuffer(0, quadVB.slice());
//#else
                pass.setVertexBuffer(0, quadVB);
//#endif
//#if MC >= 26
                pass.setIndexBuffer(quadIB, quadIBInfo.type());
//#else
                pass.setIndexBuffer(quadIB, quadIBInfo.getIndexType());
//#endif
                for (int i = 0; i < 5; i++) {
                    String samplerName = switch (i) {
                        case 0 -> "Sampler1";
                        case 1 -> "Sampler2";
                        case 2 -> "Sampler3";
                        case 3 -> "Sampler4";
                        default -> "Sampler5";
                    };
                    if (i < radii.size()) {
                        int r = radii.get(i);
//#if MC >= 26
                        if (r <= 0) pass.bindTexture(samplerName, mainFb.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
                        else pass.bindTexture(samplerName, LiquidGlassPrecomputeRuntime.get().getBlurredViewForRadius(r), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
//#else
                        if (r <= 0) pass.bindTexture(samplerName, mainFb.getColorAttachmentView(), RenderSystem.getSamplerCache().get(FilterMode.LINEAR));
                        else pass.bindTexture(samplerName, LiquidGlassPrecomputeRuntime.get().getBlurredViewForRadius(r), RenderSystem.getSamplerCache().get(FilterMode.LINEAR));
//#endif
                    } else {
                        if (!radii.isEmpty()) {
                            int r0 = radii.getFirst();
//#if MC >= 26
                            if (r0 <= 0) pass.bindTexture(samplerName, mainFb.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
                            else pass.bindTexture(samplerName, LiquidGlassPrecomputeRuntime.get().getBlurredViewForRadius(r0), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
                        } else pass.bindTexture(samplerName, mainFb.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
//#else
                            if (r0 <= 0) pass.bindTexture(samplerName, mainFb.getColorAttachmentView(), RenderSystem.getSamplerCache().get(FilterMode.LINEAR));
                            else pass.bindTexture(samplerName, LiquidGlassPrecomputeRuntime.get().getBlurredViewForRadius(r0), RenderSystem.getSamplerCache().get(FilterMode.LINEAR));
                        } else pass.bindTexture(samplerName, mainFb.getColorAttachmentView(), RenderSystem.getSamplerCache().get(FilterMode.LINEAR));
//#endif
                    }
                }
//#if MC >= 26.2
                pass.drawIndexed(6, 1, 0, 0, 0);
//#else
                pass.drawIndexed(0, 0, 6, 1);
//#endif
            }
        }
    }
}
