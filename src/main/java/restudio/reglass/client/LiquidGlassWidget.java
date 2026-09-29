package restudio.reglass.client;

//#if MC >= 26
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
//#else
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
//#endif
import restudio.reglass.client.api.ReGlassApi;
import restudio.reglass.client.api.WidgetStyle;

//#if MC >= 26
public class LiquidGlassWidget extends AbstractWidget {
//#else
public class LiquidGlassWidget extends ClickableWidget {
//#endif
    private float cornerRadiusPx;
    private boolean moveable;
    private boolean dragging;
    private int dragOffsetX;
    private int dragOffsetY;
    public WidgetStyle style = new WidgetStyle();

    public LiquidGlassWidget(int x, int y, int width, int height, WidgetStyle style) {
//#if MC >= 26
        super(x, y, width, height, Component.empty());
//#else
        super(x, y, width, height, Text.empty());
//#endif
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
//#if MC >= 26
    public void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
//#else
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
//#endif
        ReGlassApi.create(context).fromWidget(this).cornerRadius(cornerRadiusPx).style(this.style).render();
        LiquidGlassUniforms.get().tryApplyBlur(context);
    }

    @Override
//#if MC >= 26
    public boolean mouseClicked(MouseButtonEvent click, boolean isDouble) {
//#else
    public boolean mouseClicked(Click click, boolean isDouble) {
//#endif
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
//#if MC >= 26
    public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
//#else
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
//#endif
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
//#if MC >= 26
    public boolean mouseReleased(MouseButtonEvent click) {
//#else
    public boolean mouseReleased(Click click) {
//#endif
        if (this.dragging && click.button() == 0) {
            this.dragging = false;
        }

        return super.mouseReleased(click);
    }

//#if MC >= 26
    @Override protected void updateWidgetNarration(NarrationElementOutput builder) {}
//#else
    @Override protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
//#endif
}
