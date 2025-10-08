package restudio.reglass.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import restudio.reglass.client.api.ReGlassApi;
import restudio.reglass.client.api.WidgetStyle;
import restudio.reglass.client.api.model.RimLight;
import restudio.reglass.client.api.model.Tint;

public class ReGlassClient implements ClientModInitializer {
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

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (widgetToggle.wasPressed()) {
                client.setScreen(new TestScreen());
            }
        });
    }

    private static class TestScreen extends Screen {
        private boolean blur;
        private WidgetStyle customStyle;

        protected TestScreen() {
            super(Text.literal("Glass Test"));
        }

        @Override
        protected void init() {
            super.init();

            customStyle = WidgetStyle.create().tint(new Tint(Formatting.GOLD.getColorValue(), 0.4f));
            addDrawableChild(new LiquidGlassWidget(width / 2 - 75, height / 2 - 25, 150, 50, customStyle).setMoveable(true));
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            context.drawText(minecraftClient.textRenderer, Text.literal("This is a Minecraft Screen"), width / 2 - 70, 10, 0xFFFFFFFF, true);
            super.render(context, mouseX, mouseY, delta);
        }

        @Override
        public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
            if (blur) super.renderBackground(context, mouseX, mouseY, delta);
        }

        public void toggleBlur() {
            blur = !blur;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 1) {
                addDrawableChild(new LiquidGlassWidget((int) mouseX - 50, (int) mouseY - 50, 100, 100, null)).setMoveable(true);
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}