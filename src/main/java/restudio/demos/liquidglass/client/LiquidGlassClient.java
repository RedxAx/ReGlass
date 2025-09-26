package restudio.demos.liquidglass.client;

import me.x150.renderer.event.RenderEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class LiquidGlassClient implements ClientModInitializer {
    private LiquidGlassRenderer renderer;
    private static KeyBinding toggleKey;
    public static MinecraftClient minecraftClient;

    @Override
    public void onInitializeClient() {
        minecraftClient = MinecraftClient.getInstance();
        renderer = new LiquidGlassRenderer();

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.liquidglass.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.liquidglass.main"
        ));

        RenderEvents.HUD.register(context -> {
            if (toggleKey.wasPressed()) {
                renderer.toggle();
            }
            renderer.tick();
        });
    }
}