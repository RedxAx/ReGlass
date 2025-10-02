package restudio.reglass.client.gui;

import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import restudio.reglass.client.api.WidgetStyle;

public record LiquidGlassGuiElementRenderState(int x1, int y1, int x2, int y2, float cornerRadius, @Nullable Text text, WidgetStyle style) implements SpecialGuiElementRenderState {

    @Override
    public float scale() {
        return 1.0f;
    }

    @Nullable
    @Override
    public ScreenRect scissorArea() {
        return null;
    }

    @Override
    public @Nullable ScreenRect bounds() {
        return SpecialGuiElementRenderState.createBounds(x1, y1, x2, y2, scissorArea());
    }
}