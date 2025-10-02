package restudio.reglass.mixin.client;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import restudio.reglass.client.gui.LiquidGlassGuiElementRenderer;
import restudio.reglass.mixin.accessor.GuiRendererAccessor;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin {

    @Redirect(
            method = "<init>",
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;buildOrThrow()Lcom/google/common/collect/ImmutableMap;")
    )
    private ImmutableMap<Class<? extends SpecialGuiElementRenderState>, SpecialGuiElementRenderer<?>> addCustomRenderer(
            ImmutableMap.Builder<Class<? extends SpecialGuiElementRenderState>, SpecialGuiElementRenderer<?>> builder
    ) {
        GuiRenderer thisGuiRenderer = (GuiRenderer)(Object)this;
        VertexConsumerProvider.Immediate vertexConsumers = ((GuiRendererAccessor) thisGuiRenderer).getVertexConsumers();

        LiquidGlassGuiElementRenderer liquidGlassRenderer = new LiquidGlassGuiElementRenderer(vertexConsumers);
        builder.put(liquidGlassRenderer.getElementClass(), liquidGlassRenderer);

        return builder.buildOrThrow();
    }
}