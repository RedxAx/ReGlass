package restudio.reglass.client.gui;

import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import org.jetbrains.annotations.Nullable;

public record LiquidGlassGuiElementRenderState(
        int x1, int y1, int x2, int y2, float cornerRadius
) implements SpecialGuiElementRenderState {

    @Override
    public float scale() {
        return 1.0f; // We are not using the default scaling
    }

    @Nullable
    @Override
    public ScreenRect scissorArea() {
        return null; // No scissor for now
    }

    @Override
    public @Nullable ScreenRect bounds() {
        return SpecialGuiElementRenderState.createBounds(x1, y1, x2, y2, scissorArea());
    }
}