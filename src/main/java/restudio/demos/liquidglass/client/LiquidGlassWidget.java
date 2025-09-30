package restudio.demos.liquidglass.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class LiquidGlassWidget extends ClickableWidget {
    private float cornerRadiusPx;

    public LiquidGlassWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Text.empty());
        this.cornerRadiusPx = 0.5f * Math.min(width, height);
    }

    public void setCornerRadiusPx(float radiusPx) {
        this.cornerRadiusPx = Math.max(0f, radiusPx);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        LiquidGlassOverlay.get().registerWidgetRect(getX(), getY(), getWidth(), getHeight(), cornerRadiusPx);
    }

    @Override protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}