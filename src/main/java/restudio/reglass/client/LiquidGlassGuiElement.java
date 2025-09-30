package restudio.reglass.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.texture.TextureSetup;
import org.joml.Matrix3x2f;
import net.minecraft.client.render.VertexConsumer;

public class LiquidGlassGuiElement implements SimpleGuiElementRenderState {
    private final Matrix3x2f pose;
    private final int x1;
    private final int y1;
    private final int x2;
    private final int y2;
    private final ScreenRect scissor;
    private final TextureSetup textures;

    public LiquidGlassGuiElement(Matrix3x2f pose, int x, int y, int w, int h, TextureSetup textures) {
        this.pose = pose;
        this.x1 = x;
        this.y1 = y;
        this.x2 = x + w;
        this.y2 = y + h;
        this.scissor = new ScreenRect(x, y, w, h);
        this.textures = textures;
    }

    @Override
    public void setupVertices(VertexConsumer vc, float z) {
        vc.vertex(this.pose, (float) x1, (float) y1, z).texture(0f, 0f).color(255, 255, 255, 255);
        vc.vertex(this.pose, (float) x1, (float) y2, z).texture(0f, 1f).color(255, 255, 255, 255);
        vc.vertex(this.pose, (float) x2, (float) y2, z).texture(1f, 1f).color(255, 255, 255, 255);
        vc.vertex(this.pose, (float) x2, (float) y1, z).texture(1f, 0f).color(255, 255, 255, 255);
    }

    @Override
    public RenderPipeline pipeline() {
        return LiquidGlassPipelines.getGuiPipeline();
    }

    @Override
    public TextureSetup textureSetup() {
        return this.textures;
    }

    @Override
    public ScreenRect scissorArea() {
        return this.scissor;
    }

    @Override
    public ScreenRect bounds() {
        return this.scissor;
    }
}