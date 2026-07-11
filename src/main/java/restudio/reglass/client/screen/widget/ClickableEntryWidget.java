package restudio.reglass.client.screen.widget;

//? if >= 26 {
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
//? } else {
/*import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
*///? }

//? if >= 26 {
public abstract class ClickableEntryWidget<P> extends AbstractWidget {
//? } else {
/*public abstract class ClickableEntryWidget<P> extends ClickableWidget {
*///? }
    protected final P parent;

//? if >= 26 {
    public ClickableEntryWidget(P parent, int x, int y, int width, int height, Component message) {
//? } else {
    /*public ClickableEntryWidget(P parent, int x, int y, int width, int height, Text message) {
*///? }
        super(x, y, width, height, message);
        this.parent = parent;
    }
}
