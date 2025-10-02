package restudio.reglass.client;

import net.minecraft.client.gui.render.state.GuiRenderState;
import org.joml.Matrix3x2f;

public class LiquidGlassGui {
    private static final LiquidGlassGui INSTANCE = new LiquidGlassGui();
    public static LiquidGlassGui get() { return INSTANCE; }

    public void addBlob(GuiRenderState state, Matrix3x2f pose, int x, int y, int w, int h) {
        float radiusPx = 0.5f * Math.min(w, h);
        LiquidGlassUniforms.get().addRect(x, y, w, h, radiusPx);
    }
}