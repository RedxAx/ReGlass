package restudio.reglass.mixin.logical;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
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
    @Shadow @Final private BufferBuilderStorage buffers;

    @Inject(method = "render", at = @At("HEAD"))
    private void reglass$beginGuiFrame(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        LiquidGlassUniforms.get().beginFrame();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;render(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V", shift = At.Shift.AFTER))
    private void reglass$renderLiquidGlass(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        LiquidGlassUniforms uniforms = LiquidGlassUniforms.get();
        if (uniforms.getCount() <= 0) {
            return;
        }

        LiquidGlassPrecomputeRuntime.get().run();
        uniforms.uploadWidgetInfo();

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

        VertexConsumerProvider.Immediate immediate = this.buffers.getEntityVertexConsumers();
        MatrixStack matrices = new MatrixStack();
        matrices.translate(0.0, 0.0, -2000.0);
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();

        for (LiquidGlassUniforms.Widget widget : uniforms.getWidgets()) {
            TextRenderer textRenderer = this.client.textRenderer;
            float textX = widget.x() + widget.w() / 2.0F - textRenderer.getWidth(widget.text()) / 2.0F;
            float textY = widget.y() + (widget.h() - 8) / 2.0F;

            textRenderer.draw(widget.text(), textX, textY, widget.color(), widget.shadow(), matrix4f, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, 15728880);
        }
        immediate.draw();
    }
}