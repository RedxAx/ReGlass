package restudio.demos.liquidglass.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class LiquidGlassWidget extends ClickableWidget {
    public LiquidGlassWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Text.empty());
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        boolean pressed = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

        LiquidGlassOverlay.get().registerWidgetPointer(mouseX, mouseY, pressed);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}