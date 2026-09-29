package restudio.reglass.client.gui;

//#if MC >= 26
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.network.chat.Component;
//#else
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.text.Text;
//#endif
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import restudio.reglass.client.api.WidgetStyle;

//#if MC >= 26
public record LiquidGlassGuiElementRenderState(int x1, int y1, int x2, int y2, float cornerRadius, @Nullable Component text, WidgetStyle style, Matrix3x2f pose, @Nullable ScreenRectangle scissorArea, float hover, float focus) implements GuiElementRenderState {
//#else
public record LiquidGlassGuiElementRenderState(int x1, int y1, int x2, int y2, float cornerRadius, @Nullable Text text, WidgetStyle style, Matrix3x2f pose, @Nullable ScreenRect scissorArea, float hover, float focus) implements SpecialGuiElementRenderState {
//#endif

    @Override
//#if MC >= 26
    public void buildVertices(VertexConsumer vertexConsumer) {
        vertexConsumer.addVertexWith2DPose(pose, x1, y1).setColor(0);
        vertexConsumer.addVertexWith2DPose(pose, x1, y2).setColor(0);
        vertexConsumer.addVertexWith2DPose(pose, x2, y2).setColor(0);
        vertexConsumer.addVertexWith2DPose(pose, x2, y1).setColor(0);
    }

    @Override
    public RenderPipeline pipeline() {
        return RenderPipelines.GUI;
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.noTexture();
//#else
    public float scale() {
        return 1.0f;
//#endif
    }

    @Nullable
    @Override
//#if MC >= 26
    public ScreenRectangle scissorArea() {
//#else
    public ScreenRect scissorArea() {
//#endif
        return this.scissorArea;
    }

    @Override
//#if MC >= 26
    public @Nullable ScreenRectangle bounds() {
        ScreenRectangle ownBounds = new ScreenRectangle(x1, y1, x2 - x1, y2 - y1).transformMaxBounds(pose);
//#else
    public @Nullable ScreenRect bounds() {
        ScreenRect ownBounds = new ScreenRect(x1, y1, x2 - x1, y2 - y1).transformEachVertex(pose);
//#endif
        return scissorArea != null ? scissorArea.intersection(ownBounds) : ownBounds;
    }
}
