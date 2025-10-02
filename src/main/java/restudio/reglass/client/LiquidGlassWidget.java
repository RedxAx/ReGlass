package restudio.reglass.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class LiquidGlassWidget extends ClickableWidget {
    private float cornerRadiusPx;
    private boolean moveable;
    private boolean dragging;
    private int dragOffsetX;
    private int dragOffsetY;

    public LiquidGlassWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Text.empty());
        this.cornerRadiusPx = 0.5f * Math.min(width, height);
    }

    public void setCornerRadiusPx(float radiusPx) {
        this.cornerRadiusPx = Math.max(0f, radiusPx);
    }

    public void setMoveable(boolean moveable) {
        this.moveable = moveable;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        LiquidGlassUniforms.get().addWidget(getX(), getY(), getWidth(), getHeight(), this.cornerRadiusPx, getMessage(), 0xFFFFFFFF, true);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.moveable) return super.mouseClicked(mouseX, mouseY, button);
        if (button == 0 && mouseX >= this.getX() && mouseX < this.getX() + this.getWidth()
                && mouseY >= this.getY() && mouseY < this.getY() + this.getHeight()) {
            this.dragging = true;
            this.dragOffsetX = (int) (mouseX - this.getX());
            this.dragOffsetY = (int) (mouseY - this.getY());
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.dragging && button == 0) {
            int newX = (int) (mouseX - this.dragOffsetX);
            int newY = (int) (mouseY - this.dragOffsetY);
            this.setX(newX);
            this.setY(newY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.dragging && button == 0) {
            this.dragging = false;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}