package restudio.demos.liquidglass.client;

import me.x150.renderer.event.RenderEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class LiquidGlassClient implements ClientModInitializer {
    private LiquidGlassRenderer renderer;
    private static KeyBinding gameScreenToggle;
    private static KeyBinding widgetToggle;
    private static KeyBinding debugToggle;
    public static MinecraftClient minecraftClient;

    @Override
    public void onInitializeClient() {
        minecraftClient = MinecraftClient.getInstance();
        renderer = new LiquidGlassRenderer();

        gameScreenToggle = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "See In-Game Liquid Glass",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "Liquid Glass"
        ));

        widgetToggle = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Widget Based Liquid Glass",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "Liquid Glass"
        ));

        debugToggle = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Toggle Liquid Glass Debug",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                "Liquid Glass"
        ));

        RenderEvents.HUD.register(context -> {
            if (gameScreenToggle.wasPressed()) {
                renderer.toggle();
            }
            renderer.tick();
            if (widgetToggle.wasPressed()) {
                minecraftClient.setScreen(new TestScreen());
            }
        });
    }

    private static class TestScreen extends Screen {
        private LiquidGlassWidget glassWidget;

        protected TestScreen() {
            super(Text.literal("Glass Test"));
        }

        @Override
        protected void init() {
            super.init();
            if (glassWidget == null) {
                glassWidget = new LiquidGlassWidget(width, height);
            }
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);

            context.getMatrices().pushMatrix();
            float widgetX = 0;
            float widgetY = 0;
            context.getMatrices().translate(widgetX, widgetY);
            glassWidget.render(context, (int)(mouseX - widgetX), (int)(mouseY - widgetY), delta);
            context.getMatrices().popMatrix();
        }

        @Override
        public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (debugToggle.matchesKey(keyCode, scanCode)) {
                if (glassWidget != null) {
                    glassWidget.toggleDebugMode();
                    return true;
                }
            }
            if (widgetToggle.matchesKey(keyCode, scanCode)) {
                close();
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public void close() {
            if (glassWidget != null) {
                glassWidget.close();
                glassWidget = null;
            }
            super.close();
        }
    }
}