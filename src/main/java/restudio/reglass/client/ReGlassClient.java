package restudio.reglass.client;

//#if MC < 26
import java.util.Arrays;
//#endif
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//#if MC >= 26
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
//#if MC < 26.2
import net.minecraft.ChatFormatting;
//#endif
import net.minecraft.resources.Identifier;
//#else
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
//#endif
import org.lwjgl.glfw.GLFW;
import restudio.reglass.client.api.ReGlassConfig;
import restudio.reglass.client.api.WidgetStyle;
import restudio.reglass.client.config.ReGlassSettingsIO;
//#if MC < 26
import restudio.reglass.client.screen.config.ReGlassConfigScreen;
//#endif
import restudio.reglass.mixin.accessor.OptionsAccessor;
//#if MC >= 26
import restudio.reglass.client.screen.config.ReGlassConfigScreen;

import java.util.Arrays;
//#endif

public class ReGlassClient implements ClientModInitializer {
//#if MC >= 26
    private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("reglass", "main"));
    private static final KeyMapping CONFIG_KEY = new KeyMapping("key.reglass.config", InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);
    private static final KeyMapping PLAYGROUND_KEY = new KeyMapping("key.reglass.playground", InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);
    private static final KeyMapping TOGGLE_REDESIGN_KEY = new KeyMapping("key.reglass.toggle_redesign", InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);
//#else
    private static final KeyBinding.Category KEY_CATEGORY = KeyBinding.Category.create(Identifier.of("reglass", "main"));
    private static final KeyBinding CONFIG_KEY = new KeyBinding("key.reglass.config", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, KEY_CATEGORY);
    private static final KeyBinding PLAYGROUND_KEY = new KeyBinding("key.reglass.playground", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, KEY_CATEGORY);
    private static final KeyBinding TOGGLE_REDESIGN_KEY = new KeyBinding("key.reglass.toggle_redesign", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, KEY_CATEGORY);
//#endif
    private static boolean keyMappingsRegistered;
    private static boolean toggleRedesignWasDown;

//#if MC >= 26
    public static Minecraft minecraftClient;
//#else
    public static MinecraftClient minecraftClient;
//#endif

    @Override
    public void onInitializeClient() {
//#if MC >= 26
        minecraftClient = Minecraft.getInstance();
//#else
        minecraftClient = MinecraftClient.getInstance();
//#endif

        ReGlassSettingsIO.loadIntoMemory();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!keyMappingsRegistered) {
                if (client.options == null) {
                    return;
                }
                registerKeyMappings(client);
            }
            handleGlobalToggleRedesignKey(client);
//#if MC >= 26.2
            while (CONFIG_KEY.consumeClick()) {
                if (client.gui.screen() == null) {
//#elseif MC >= 26
            while (CONFIG_KEY.consumeClick()) {
                if (client.screen == null) {
//#else
            while (CONFIG_KEY.wasPressed()) {
                if (client.currentScreen == null) {
//#endif
//#if MC >= 26.2
                    client.gui.setScreen(new ReGlassConfigScreen(null));
//#else
                    client.setScreen(new ReGlassConfigScreen(null));
//#endif
                }
            }
//#if MC >= 26.2
            while (PLAYGROUND_KEY.consumeClick()) {
                if (client.gui.screen() == null) {
//#elseif MC >= 26
            while (PLAYGROUND_KEY.consumeClick()) {
                if (client.screen == null) {
//#else
            while (PLAYGROUND_KEY.wasPressed()) {
                if (client.currentScreen == null) {
//#endif
//#if MC >= 26.2
                    client.gui.setScreen(new PlaygroundScreen());
//#else
                    client.setScreen(new PlaygroundScreen());
//#endif
                }
            }
        });
    }

//#if MC >= 26
    private static void registerKeyMappings(Minecraft client) {
        KeyMapping[] existingMappings = client.options.keyMappings;
        KeyMapping[] mappings = Arrays.copyOf(existingMappings, existingMappings.length + 3);
//#else
    private static void registerKeyMappings(MinecraftClient client) {
        KeyBinding[] existingMappings = client.options.allKeys;
        KeyBinding[] mappings = Arrays.copyOf(existingMappings, existingMappings.length + 3);
//#endif
        mappings[existingMappings.length] = CONFIG_KEY;
        mappings[existingMappings.length + 1] = PLAYGROUND_KEY;
        mappings[existingMappings.length + 2] = TOGGLE_REDESIGN_KEY;
        ((OptionsAccessor) client.options).setKeyMappings(mappings);
//#if MC >= 26
        KeyMapping.resetMapping();
//#else
        KeyBinding.updateKeysByCode();
//#endif
        client.options.load();
        keyMappingsRegistered = true;
    }

//#if MC >= 26
    private static void handleGlobalToggleRedesignKey(Minecraft client) {
//#else
    private static void handleGlobalToggleRedesignKey(MinecraftClient client) {
//#endif
        boolean down = isGlobalKeyDown(client, TOGGLE_REDESIGN_KEY);
        if (down && !toggleRedesignWasDown) {
            ReGlassConfig.INSTANCE.features.enableRedesign = !ReGlassConfig.INSTANCE.features.enableRedesign;
            ReGlassSettingsIO.saveFromMemory();
        }
        toggleRedesignWasDown = down;
    }

//#if MC >= 26
    private static boolean isGlobalKeyDown(Minecraft client, KeyMapping keyMapping) {
        if (keyMapping.isUnbound()) {
//#else
    private static boolean isGlobalKeyDown(MinecraftClient client, KeyBinding keyBinding) {
        if (keyBinding.isUnbound()) {
//#endif
            return false;
        }
//#if MC >= 26
        InputConstants.Key key = InputConstants.getKey(keyMapping.saveString());
        if (key.getType() == InputConstants.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(client.getWindow().handle(), key.getValue()) == GLFW.GLFW_PRESS;
//#else
        InputUtil.Key key = InputUtil.fromTranslationKey(keyBinding.getBoundKeyTranslationKey());
        if (key.getCategory() == InputUtil.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(client.getWindow().getHandle(), key.getCode()) == GLFW.GLFW_PRESS;
//#endif
        }
//#if MC >= 26
        return InputConstants.isKeyDown(client.getWindow(), key.getValue());
//#else
        return InputUtil.isKeyPressed(client.getWindow(), key.getCode());
//#endif
    }

    public static class PlaygroundScreen extends Screen {
        private boolean blur;

        public PlaygroundScreen() {
//#if MC >= 26
            super(Component.literal("ReGlass Playground"));
//#else
            super(Text.literal("ReGlass Playground"));
//#endif
        }

        @Override
        protected void init() {
            super.init();

//#if MC >= 26.2
            WidgetStyle customStyle = WidgetStyle.create().tint(0xFFAA00, 0.4f).blurRadius(0).shadow(25f, 0.2f, 0f, 3f).smoothing(.05f).shadowColor(0x000000, 1.0f);
            addRenderableWidget(new LiquidGlassWidget(width / 2 - 75, height / 2 - 25, 150, 50, customStyle).setMoveable(true));
//#elseif MC >= 26
            WidgetStyle customStyle = WidgetStyle.create().tint(ChatFormatting.GOLD.getColor(), 0.4f).blurRadius(0).shadow(25f, 0.2f, 0f, 3f).smoothing(.05f).shadowColor(0x000000, 1.0f);
            addRenderableWidget(new LiquidGlassWidget(width / 2 - 75, height / 2 - 25, 150, 50, customStyle).setMoveable(true));
            addRenderableWidget(Button.builder(Component.literal("Toggle BG Blur"), b -> blur = !blur).bounds(10, 10, 120, 20).build());
//#else
            WidgetStyle customStyle = WidgetStyle.create().tint(Formatting.GOLD.getColorValue(), 0.4f).blurRadius(0).shadow(25f, 0.2f, 0f, 3f).smoothing(.05f).shadowColor(0x000000, 1.0f);
            addDrawableChild(new LiquidGlassWidget(width / 2 - 75, height / 2 - 25, 150, 50, customStyle).setMoveable(true));
            addDrawableChild(ButtonWidget.builder(Text.literal("Toggle BG Blur"), b -> blur = !blur).dimensions(10, 10, 120, 20).build());
//#endif
        }

        @Override
//#if MC >= 26
        public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
            context.text(minecraftClient.font, Component.literal("This is a Minecraft Screen"), width / 2 - 70, 10, 0xFFFFFFFF, true);
            super.extractRenderState(context, mouseX, mouseY, delta);
//#else
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            context.drawText(minecraftClient.textRenderer, Text.literal("This is a Minecraft Screen"), width / 2 - 70, 10, 0xFFFFFFFF, true);
            super.render(context, mouseX, mouseY, delta);
//#endif
        }

        @Override
//#if MC >= 26
        public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
            if (blur) super.extractBackground(context, mouseX, mouseY, delta);
//#else
        public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
            if (blur) super.renderBackground(context, mouseX, mouseY, delta);
//#endif
        }

        @Override
//#if MC >= 26
        public boolean mouseClicked(MouseButtonEvent click, boolean isDouble) {
//#else
        public boolean mouseClicked(Click click, boolean isDouble) {
//#endif
            if (click.button() == 1) {
//#if MC >= 26
                addRenderableWidget(new LiquidGlassWidget((int) click.x() - 50, (int) click.y() - 50, 100, 100, WidgetStyle.create().smoothing(.05f))).setMoveable(true);
//#else
                addDrawableChild(new LiquidGlassWidget((int) click.x() - 50, (int) click.y() - 50, 100, 100, WidgetStyle.create().smoothing(.05f))).setMoveable(true);
//#endif
                return true;
            }
            return super.mouseClicked(click, isDouble);
        }
    }
}

