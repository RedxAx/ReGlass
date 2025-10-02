package restudio.reglass.mixin.logical;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.LiquidGlassPipelines;
import restudio.reglass.client.LiquidGlassPrecomputeRuntime;
import restudio.reglass.client.LiquidGlassTextManager;
import restudio.reglass.client.LiquidGlassUniforms;

import java.util.OptionalDouble;
import java.util.OptionalInt;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private GuiRenderState guiState;
    @Shadow @Final private GuiRenderer guiRenderer;
    @Shadow @Final private FogRenderer fogRenderer;

    @Inject(method = "render", at = @At("HEAD"))
    private void reglass$beginGuiFrame(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        LiquidGlassUniforms.get().beginFrame();
        LiquidGlassTextManager.getInstance().clear();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;render(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V", shift = At.Shift.AFTER))
    private void reglass$renderLiquidGlass(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        if (LiquidGlassUniforms.get().getCount() <= 0) {
            return;
        }

        LiquidGlassPrecomputeRuntime.get().run();
        LiquidGlassUniforms.get().uploadWidgetInfo();

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
            pass.setUniform("SamplerInfo", LiquidGlassUniforms.get().getSamplerInfoBuffer());
            pass.setUniform("CustomUniforms", LiquidGlassUniforms.get().getCustomUniformsBuffer());
            pass.setUniform("WidgetInfo", LiquidGlassUniforms.get().getWidgetInfoBuffer());

            pass.bindSampler("Sampler0", mainFb.getColorAttachmentView());
            pass.bindSampler("Sampler1", LiquidGlassPrecomputeRuntime.get().getBlurredView());
            pass.bindSampler("Sampler2", LiquidGlassPrecomputeRuntime.get().getBloomView());

            GpuBuffer quadVB = RenderSystem.getQuadVertexBuffer();
            RenderSystem.ShapeIndexBuffer quadIBInfo = RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS);
            GpuBuffer quadIB = quadIBInfo.getIndexBuffer(6);

            pass.setVertexBuffer(0, quadVB);
            pass.setIndexBuffer(quadIB, quadIBInfo.getIndexType());
            pass.drawIndexed(0, 0, 6, 1);
        }

        DrawContext drawContext = new DrawContext(this.client, this.guiState);
        LiquidGlassTextManager.getInstance().renderAll(drawContext);
        GpuBufferSlice fogBuffer = this.fogRenderer.getFogBuffer(FogRenderer.FogType.WORLD);
        this.guiRenderer.render(fogBuffer);
    }
}