package restudio.reglass.client;

import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.texture.TextureSetup;
import org.joml.Matrix3x2f;

public class LiquidGlassGui {
    private static final LiquidGlassGui INSTANCE = new LiquidGlassGui();
    public static LiquidGlassGui get() { return INSTANCE; }

    public void addBlob(GuiRenderState state, Matrix3x2f pose, int x, int y, int w, int h) {
        MinecraftClient mc = MinecraftClient.getInstance();
        GpuTextureView main = mc.getFramebuffer().getColorAttachmentView();
        if (main == null) return;

        GpuTextureView blurred = LiquidGlassPrecomputeRuntime.get().getBlurredView();
        GpuTextureView bloom = LiquidGlassPrecomputeRuntime.get().getBloomView();

        if (blurred == null || bloom == null) return;

        TextureSetup textures = new TextureSetup(main, blurred, bloom);

        float radiusPx = 0.5f * Math.min(w, h);
        LiquidGlassUniforms.get().addRect(x, y, w, h, radiusPx);
        LiquidGlassUniforms.get().uploadWidgetInfo();

        LiquidGlassGuiElement elem = new LiquidGlassGuiElement(pose, x, y, w, h, textures);
        state.addSimpleElement(elem);
    }
}