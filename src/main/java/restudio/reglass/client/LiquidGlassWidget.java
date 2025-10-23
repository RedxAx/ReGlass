package restudio.reglass.client;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import restudio.reglass.client.api.ReGlassApi;
import restudio.reglass.client.api.WidgetStyle;

public class LiquidGlassWidget extends ClickableWidget {
    private float cornerRadiusPx;
    private boolean moveable;
    private boolean dragging;
    private int dragOffsetX;
    private int dragOffsetY;
    public WidgetStyle style = new WidgetStyle();

    public LiquidGlassWidget(int x, int y, int width, int height, WidgetStyle style) {
        super(x, y, width, height, Text.empty());
        this.cornerRadiusPx = 0.5f * Math.min(width, height);
        if (style != null) this.style = style;
    }

    public LiquidGlassWidget setCornerRadiusPx(float radiusPx) {
        this.cornerRadiusPx = Math.max(0f, radiusPx);
        return this;
    }

    public LiquidGlassWidget setMoveable(boolean moveable) {
        this.moveable = moveable;
        return this;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        ReGlassApi.create(context).fromWidget(this).cornerRadius(cornerRadiusPx).style(this.style).render();
        LiquidGlassUniforms.get().tryApplyBlur(context);
    }

    @Override
    public boolean mouseClicked(Click click, boolean isDouble) {
        if (!this.moveable) return super.mouseClicked(click, isDouble);
        if (click.button() == 0 && click.x() >= this.getX() && click.x() < this.getX() + this.getWidth() && click.y() >= this.getY() && click.y() < this.getY() + this.getHeight()) {
            this.dragging = true;
            this.dragOffsetX = (int) (click.x() - this.getX());
            this.dragOffsetY = (int) (click.y() - this.getY());
            return true;
        }
        return super.mouseClicked(click, isDouble);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (this.dragging && click.button() == 0) {
            int newX = (int) (click.x() - this.dragOffsetX);
            int newY = (int) (click.y() - this.dragOffsetY);
            this.setX(newX);
            this.setY(newY);
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (this.dragging && click.button() == 0) {
            this.dragging = false;
        }

        return super.mouseReleased(click);
    }

    @Override protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}