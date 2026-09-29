package restudio.reglass.client.screen.widget;

//#if MC >= 26
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
//#else
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
//#endif

//#if MC >= 26
public abstract class ClickableEntryWidget<P> extends AbstractWidget {
//#else
public abstract class ClickableEntryWidget<P> extends ClickableWidget {
//#endif
    protected final P parent;

//#if MC >= 26
    public ClickableEntryWidget(P parent, int x, int y, int width, int height, Component message) {
//#else
    public ClickableEntryWidget(P parent, int x, int y, int width, int height, Text message) {
//#endif
        super(x, y, width, height, message);
        this.parent = parent;
    }
}
