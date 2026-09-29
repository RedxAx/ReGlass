package restudio.reglass.client.gui;

import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
//#if MC >= 26
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
//#else
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
//#endif
import restudio.reglass.client.LiquidGlassUniforms;

public class LiquidGlassGuiElementRenderer extends SpecialGuiElementRenderer<LiquidGlassGuiElementRenderState> {

//#if MC >= 26
    public LiquidGlassGuiElementRenderer(MultiBufferSource.BufferSource vertexConsumers) {
//#else
    public LiquidGlassGuiElementRenderer(VertexConsumerProvider.Immediate vertexConsumers) {
//#endif
        super(vertexConsumers);
    }

    @Override
//#if MC >= 26
    public void extractRenderState(LiquidGlassGuiElementRenderState element, GuiRenderState state, int scale) {
//#else
    public void render(LiquidGlassGuiElementRenderState element, GuiRenderState state, int scale) {
//#endif
        LiquidGlassUniforms.get().addWidget(element);
    }

    @Override
    public Class<LiquidGlassGuiElementRenderState> getElementClass() {
        return LiquidGlassGuiElementRenderState.class;
    }

    @Override
//#if MC >= 26
    protected void render(LiquidGlassGuiElementRenderState element, PoseStack matrices) {
//#else
    protected void render(LiquidGlassGuiElementRenderState element, MatrixStack matrices) {
//#endif
    }

    @Override
    protected String getName() {
        return "liquid_glass_widget";
    }
}
