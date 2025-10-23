package restudio.reglass.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import restudio.reglass.client.api.WidgetStyle;
import restudio.reglass.client.config.ReGlassSettingsIO;
import restudio.reglass.client.screen.config.ReGlassConfigScreen;

public class ReGlassClient implements ClientModInitializer {
    private static KeyBinding playgroundKey;
    private static KeyBinding configKey;
    public static KeyBinding.Category reGlassCategory = new KeyBinding.Category(Identifier.of("reglass", "category"));

    public static MinecraftClient minecraftClient;

    @Override
    public void onInitializeClient() {
        minecraftClient = MinecraftClient.getInstance();

        playgroundKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("ReGlass Playground", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, reGlassCategory));
        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("ReGlass Config", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, reGlassCategory));

        ReGlassSettingsIO.loadIntoMemory();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (configKey.wasPressed()) {
                client.setScreen(new ReGlassConfigScreen(null));
            }
            if (playgroundKey.wasPressed()) {
                client.setScreen(new PlaygroundScreen());
            }
        });
    }

    public static class PlaygroundScreen extends Screen {
        private boolean blur;
        private WidgetStyle customStyle;

        public PlaygroundScreen() {
            super(Text.literal("ReGlass Playground"));
        }

        @Override
        protected void init() {
            super.init();

            customStyle = WidgetStyle.create().tint(Formatting.GOLD.getColorValue(), 0.4f).blurRadius(0).shadow(25f, 0.2f, 0f, 3f).smoothing(.05f).shadowColor(0x000000, 1.0f);
            addDrawableChild(new LiquidGlassWidget(width / 2 - 75, height / 2 - 25, 150, 50, customStyle).setMoveable(true));
            addDrawableChild(ButtonWidget.builder(Text.literal("Toggle BG Blur"), b -> blur = !blur).dimensions(10, 10, 120, 20).build());
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

        @Override
        public boolean mouseClicked(Click click, boolean isDouble) {
            if (click.button() == 1) {
                addDrawableChild(new LiquidGlassWidget((int) click.x() - 50, (int) click.y() - 50, 100, 100, WidgetStyle.create().smoothing(.05f))).setMoveable(true);
                return true;
            }
            return super.mouseClicked(click, isDouble);
        }
    }
}