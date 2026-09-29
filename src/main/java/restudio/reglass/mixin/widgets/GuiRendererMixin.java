package restudio.reglass.mixin.widgets;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;
import net.minecraft.client.gui.render.GuiRenderer;
//#if MC >= 26.2
import net.minecraft.client.renderer.state.gui.GuiRenderState;
//#elseif MC >= 26
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
//#else
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
//#endif
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.gui.QuadVertexBufferProvider;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin implements QuadVertexBufferProvider {
    @Unique
    private GpuBuffer reglass$quadVertexBuffer;

    @Inject(method = "<init>", at = @At("TAIL"))
//#if MC >= 26.2
    private void reglass$onInit(GuiRenderState state, net.minecraft.client.renderer.feature.FeatureRenderDispatcher dispatcher, List specialElementRenderers, CallbackInfo ci) {
//#elseif MC >= 26
    private void reglass$onInit(GuiRenderState state, MultiBufferSource.BufferSource vertexConsumers, SubmitNodeCollector queue, net.minecraft.client.renderer.feature.FeatureRenderDispatcher dispatcher, List specialElementRenderers, CallbackInfo ci) {
//#else
    private void reglass$onInit(GuiRenderState state, VertexConsumerProvider.Immediate vertexConsumers, OrderedRenderCommandQueue queue, net.minecraft.client.render.command.RenderDispatcher dispatcher, List specialElementRenderers, CallbackInfo ci) {
//#endif
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer byteBuffer = stack.malloc(4 * 3 * 4);
            FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
            floatBuffer.put(new float[]{
                -1.0f, -1.0f, 0.0f,
                 1.0f, -1.0f, 0.0f,
                 1.0f,  1.0f, 0.0f,
                -1.0f,  1.0f, 0.0f
            });
            byteBuffer.rewind();
            this.reglass$quadVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "reglass_quad_vbo", 32, byteBuffer);
        }
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void reglass$onClose(CallbackInfo ci) {
        if (this.reglass$quadVertexBuffer != null) {
            this.reglass$quadVertexBuffer.close();
        }
    }

    @Unique
    public GpuBuffer getQuadVertexBuffer() {
        return this.reglass$quadVertexBuffer;
    }
}
