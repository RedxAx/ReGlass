package restudio.reglass.client.runtime;

//#if MC >= 26.3
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import org.lwjgl.sdl.SDLMouse;
//#elseif MC >= 26
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
//#else
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
//#endif

/** Native input differences between Minecraft's SDL and GLFW clients. */
public final class PlatformInput {
//#if MC >= 26.3
    public static final int LEFT_MOUSE_BUTTON = InputConstants.MOUSE_BUTTON_LEFT;
    public static final int RIGHT_MOUSE_BUTTON = InputConstants.MOUSE_BUTTON_RIGHT;
//#else
    public static final int LEFT_MOUSE_BUTTON = GLFW.GLFW_MOUSE_BUTTON_LEFT;
    public static final int RIGHT_MOUSE_BUTTON = GLFW.GLFW_MOUSE_BUTTON_RIGHT;
//#endif

    private PlatformInput() {}

    public static boolean isMouseButtonDown(int button) {
//#if MC >= 26.3
        return button >= 1 && button <= 32
                && (SDLMouse.SDL_GetMouseState(null, null) & (1 << (button - 1))) != 0;
//#elseif MC >= 26
        return GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().handle(), button) == GLFW.GLFW_PRESS;
//#else
        return GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), button) == GLFW.GLFW_PRESS;
//#endif
    }

    public static boolean isWindowFocused() {
//#if MC >= 26.3
        return Minecraft.getInstance().getWindow().isFocused();
//#elseif MC >= 26
        return GLFW.glfwGetWindowAttrib(Minecraft.getInstance().getWindow().handle(), GLFW.GLFW_FOCUSED) == GLFW.GLFW_TRUE;
//#else
        return GLFW.glfwGetWindowAttrib(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_FOCUSED) == GLFW.GLFW_TRUE;
//#endif
    }
}
