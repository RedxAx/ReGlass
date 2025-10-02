package restudio.reglass.mixin.logical;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
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

        VertexConsumerProvider.Immediate immediate = this.buffers.getEffectVertexConsumers();
        MatrixStack matrices = new MatrixStack();
        matrices.translate(0.0, 0.0, -2000.0);

        for (LiquidGlassUniforms.Widget widget : uniforms.getWidgets()) {
            TextRenderer textRenderer = this.client.textRenderer;
            Text message = widget.text();
            int textWidth = textRenderer.getWidth(message);
            int textX = (int)(widget.x() + widget.w() / 2 - textWidth / 2);
            int textY = (int)(widget.y() + (widget.h() - 8) / 2);

            LiquidGlassUniforms.IconInfo iconInfo = widget.iconInfo();
            if (iconInfo != null) {
                int iconAndTextWidth;
                int iconTextSpacing = 0;
                boolean hasText = !message.getString().isEmpty();

                if (hasText) {
                    iconTextSpacing = 3;
                    iconAndTextWidth = iconInfo.texWidth() + iconTextSpacing + textWidth;
                } else {
                    iconAndTextWidth = iconInfo.texWidth();
                }

                int iconX = (int)(widget.x() + (widget.w() - iconAndTextWidth) / 2);
                int iconY = (int)(widget.y() + (widget.h() - iconInfo.texHeight()) / 2);

                if (hasText) {
                    textX = iconX + iconInfo.texWidth() + iconTextSpacing;
                }

                RenderLayer renderLayer = RenderLayer.getTextSeeThrough(iconInfo.texture());
                VertexConsumer buffer = immediate.getBuffer(renderLayer);
                Matrix4f matrix = matrices.peek().getPositionMatrix();

                float x1 = (float)iconX;
                float y1 = (float)iconY;
                float x2 = (float)(iconX + iconInfo.texWidth());
                float y2 = (float)(iconY + iconInfo.texHeight());

                float u1 = 0.0F;
                float v1 = 0.0F;
                float u2 = 1.0F;
                float v2 = 1.0F;

                buffer.vertex(matrix, x1, y2, 0.0f).texture(u1, v2).color(255, 255, 255, 255).light(15728880);
                buffer.vertex(matrix, x2, y2, 0.0f).texture(u2, v2).color(255, 255, 255, 255).light(15728880);
                buffer.vertex(matrix, x2, y1, 0.0f).texture(u2, v1).color(255, 255, 255, 255).light(15728880);
                buffer.vertex(matrix, x1, y1, 0.0f).texture(u1, v1).color(255, 255, 255, 255).light(15728880);
            }

            if (!message.getString().isEmpty()) {
                Matrix4f matrix = matrices.peek().getPositionMatrix();
                textRenderer.draw(message, (float)textX, (float)textY, widget.color(), widget.shadow(), matrix, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, 15728880);
            }
        }

        immediate.draw();
    }
}