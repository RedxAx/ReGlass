package restudio.reglass.mixin.accessor;

import net.minecraft.client.gui.render.GuiRenderer;
//? if >= 26 {
import net.minecraft.client.renderer.MultiBufferSource;
//? } else {
/*import net.minecraft.client.render.VertexConsumerProvider;
*///? }
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiRenderer.class)
public interface GuiRendererAccessor {

    @Accessor("vertexConsumers")
//? if >= 26 {
    MultiBufferSource.BufferSource getVertexConsumers();
//? } else {
    /*VertexConsumerProvider.Immediate getVertexConsumers();
*///? }
}

