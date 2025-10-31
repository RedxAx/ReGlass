package restudio.reglass.client.gui;

import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import restudio.reglass.client.api.WidgetStyle;

public record LiquidGlassGuiElementRenderState(int x1, int y1, int x2, int y2, float cornerRadius, @Nullable Text text, WidgetStyle style, Matrix3x2f pose, @Nullable ScreenRect scissorArea, float hover, float focus) implements SpecialGuiElementRenderState {

    @Override
    public float scale() {
        return 1.0f;
    }

    @Nullable
    @Override
    public ScreenRect scissorArea() {
        return this.scissorArea;
    }

    @Override
    public @Nullable ScreenRect bounds() {
        ScreenRect ownBounds = new ScreenRect(x1, y1, x2 - x1, y2 - y1).transformEachVertex(pose);
        return scissorArea != null ? scissorArea.intersection(ownBounds) : ownBounds;
    }
}