package restudio.reglass.client.gui;

import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import restudio.reglass.client.LiquidGlassUniforms;

public class LiquidGlassGuiElementRenderer extends SpecialGuiElementRenderer<LiquidGlassGuiElementRenderState> {

    public LiquidGlassGuiElementRenderer(VertexConsumerProvider.Immediate vertexConsumers) {
        super(vertexConsumers);
    }

    @Override
    public void render(LiquidGlassGuiElementRenderState element, GuiRenderState state, int scale) {
        //addWidget(float x, float y, float w, float h, float radiusPx, int color, boolean shadow, IconInfo iconInfo)
        LiquidGlassUniforms.get().addWidget(
                element.x1(), element.y1(),
                element.x2() - element.x1(), element.y2() - element.y1(),
                element.cornerRadius(), Text.empty(),
                0xFFFFFFFF, false, null
        );
    }

    @Override
    public Class<LiquidGlassGuiElementRenderState> getElementClass() {
        return LiquidGlassGuiElementRenderState.class;
    }

    @Override
    protected void render(LiquidGlassGuiElementRenderState element, MatrixStack matrices) {
    }

    @Override
    protected String getName() {
        return "liquid_glass_widget";
    }
}