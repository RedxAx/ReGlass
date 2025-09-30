package restudio.demos.liquidglass.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import me.x150.renderer.event.RenderEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

public class LiquidGlassClient implements ClientModInitializer {
    private static KeyBinding widgetToggle;
    public static MinecraftClient minecraftClient;

    @Override
    public void onInitializeClient() {
        minecraftClient = MinecraftClient.getInstance();

        widgetToggle = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Widget Based Liquid Glass",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "Liquid Glass"
        ));

        RenderEvents.HUD.register(context -> {
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

            addDrawableChild(ButtonWidget.builder(Text.literal("Minecraft Button"), button -> close())
                    .dimensions(width / 2 - 50, height - 30, 100, 20).build());

            if (glassWidget == null) {
                glassWidget = new LiquidGlassWidget(0, 0, 24, 24);
            }
            addDrawableChild(glassWidget);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                addDrawableChild(new LiquidGlassWidget((int) mouseX - 50, (int) mouseY - 50, 100, 100));
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            context.drawText(minecraftClient.textRenderer, Text.literal("This is a Minecraft Screen"), width / 2 - 70, 10, 0xFFFFFFFF, true);
            super.render(context, mouseX, mouseY, delta);
        }

        @Override
        public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        }
    }
}