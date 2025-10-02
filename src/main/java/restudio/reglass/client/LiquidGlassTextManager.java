package restudio.reglass.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class LiquidGlassTextManager {
    private static final LiquidGlassTextManager INSTANCE = new LiquidGlassTextManager();
    private final List<TextRenderInfo> textsToRender = new ArrayList<>();

    private LiquidGlassTextManager() {}

    public static LiquidGlassTextManager getInstance() {
        return INSTANCE;
    }

    public void addText(Text text, int x, int y) {
        textsToRender.add(new TextRenderInfo(text, x, y));
    }

    public void renderAll(DrawContext context) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        for (TextRenderInfo info : textsToRender) {
            context.drawCenteredTextWithShadow(textRenderer, info.text, info.x, info.y, 0xFFFFFFFF);
        }
    }

    public void clear() {
        textsToRender.clear();
    }

    private record TextRenderInfo(Text text, int x, int y) {}
}