package restudio.reglass.client.screen.widget;

import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public abstract class ClickableEntryWidget<P> extends ClickableWidget {
    protected final P parent;

    public ClickableEntryWidget(P parent, int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
        this.parent = parent;
    }
}