package restudio.reglass.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import restudio.reglass.client.gui.LiquidGlassGuiElementRenderState;

public class LiquidGlassWidget extends ClickableWidget {
    private float cornerRadiusPx;

    public LiquidGlassWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Text.empty());
        this.cornerRadiusPx = 0.5f * Math.min(width, height);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.state.addSpecialElement(new LiquidGlassGuiElementRenderState(getX(), getY(), getX() + getWidth(), getY() + getHeight(), this.cornerRadiusPx));
    }

    @Override protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}