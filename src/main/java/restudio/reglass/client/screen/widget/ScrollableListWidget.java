package restudio.reglass.client.screen.widget;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.Set;

public class ScrollableListWidget<E extends ScrollableListWidget.Entry<E>> extends ClickableEntryWidget<Screen> {
    protected final int itemHeight;
    private int verticalPadding = 0;
    private final List<E> entries = Lists.newArrayList();
    private final Set<E> selectedEntries = Sets.newHashSet();
    private double scrollAmount;

    public ScrollableListWidget(Screen screen, int x, int y, int width, int height, int itemHeight) {
        super(screen, x, y, width, height, Text.empty());
        this.itemHeight = itemHeight;
    }

    public void setVerticalPadding(int padding) {
        this.verticalPadding = padding;
    }

    public int getVerticalPadding() {
        return this.verticalPadding;
    }

    public void addEntry(E entry) {
        this.entries.add(entry);
    }

    public void clearEntries() {
        this.entries.forEach(Entry::close);
        this.entries.clear();
        this.selectedEntries.clear();
    }

    public List<E> getEntries() {
        return this.entries;
    }

    public Set<E> getSelectedEntries() {
        return this.selectedEntries;
    }

    public void setSelected(E entry) {
        if (!InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow(), InputUtil.GLFW_KEY_LEFT_CONTROL)) {
            this.selectedEntries.clear();
        }

        if (this.selectedEntries.contains(entry)) {
            this.selectedEntries.remove(entry);
        } else {
            this.selectedEntries.add(entry);
        }
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.enableScissor(getX(), getY(), getX() + width, getY() + height);

        int top = getY() - (int) this.scrollAmount + verticalPadding;
        for (int i = 0; i < this.entries.size(); i++) {
            E entry = this.entries.get(i);
            int entryY = top + i * (this.itemHeight + verticalPadding);
            if (entryY + this.itemHeight >= this.getY() && entryY <= this.getY() + this.height) {
                boolean isHovered = isMouseOver(mouseX, mouseY) && mouseY >= entryY && mouseY < entryY + this.itemHeight;
                entry.render(context, i, getX(), entryY, width, this.itemHeight, mouseX, mouseY, isHovered, delta);
            }
        }

        context.disableScissor();
    }

    @Override
    public boolean mouseClicked(Click button, boolean isDouble) {
        if (isMouseOver(button.y(), button.y())) {
            int top = getY() - (int) this.scrollAmount + verticalPadding;
            for (int i = 0; i < this.entries.size(); i++) {
                E entry = this.entries.get(i);
                int entryY = top + i * (this.itemHeight + verticalPadding);
                if (button.y() >= entryY && button.y() < entryY + this.itemHeight) {
                    if (entry.mouseClicked(button)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }



    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (isMouseOver(mouseX, mouseY)) {
            this.scrollAmount -= verticalAmount * (this.itemHeight / 2.0);
            this.scrollAmount = MathHelper.clamp(this.scrollAmount, 0, Math.max(0, this.getMaxScroll()));
            return true;
        }
        return false;
    }

    private int getMaxScroll() {
        return this.entries.size() * (this.itemHeight + verticalPadding) - verticalPadding - this.height;
    }

    @Override
    public void setFocused(boolean focused) {}

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }

    public static abstract class Entry<E extends Entry<E>> {
        protected int x, y, width, height;

        public Entry(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public void render(DrawContext context, int index, int x, int y, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public abstract boolean mouseClicked(Click button);

        public boolean isMouseOver(double mouseX, double mouseY) {
            return mouseX >= this.x && mouseX < this.x + this.width &&
                   mouseY >= this.y && mouseY < this.y + this.height;
        }

        public void close() {}
    }
}