package restudio.reglass.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import restudio.reglass.mixin.accessor.GuiRenderStateAccessor;

import java.util.ArrayList;
import java.util.List;

public final class LiquidGlassUniforms {

    private static final LiquidGlassUniforms INSTANCE = new LiquidGlassUniforms();
    public static LiquidGlassUniforms get() { return INSTANCE; }

    public record IconInfo(Identifier texture, int texU, int texV, int texWidth, int texHeight) {}
    public record Widget(float x, float y, float w, float h, float radiusPx, Text text, int color, boolean shadow, @Nullable IconInfo iconInfo) {}

    private final GpuBuffer samplerInfo;
    private final GpuBuffer customUniforms;
    private final GpuBuffer widgetInfo;

    private static final int MAX_WIDGETS = 64;
    private final List<Widget> widgets = new ArrayList<>();
    private boolean screenWantsBlur = false;

    private LiquidGlassUniforms() {
        samplerInfo    = RenderSystem.getDevice().createBuffer(() -> "reglass SamplerInfo", 130, 16);
        customUniforms = RenderSystem.getDevice().createBuffer(() -> "reglass CustomUniforms", 130, 48);
        int widgetInfoSize = 16 * (1 + MAX_WIDGETS + MAX_WIDGETS);
        widgetInfo     = RenderSystem.getDevice().createBuffer(() -> "reglass WidgetInfo", 130, widgetInfoSize);
    }

    public void beginFrame() {
        widgets.clear();
        screenWantsBlur = false;
    }

    public void setScreenWantsBlur(boolean wantsBlur) {
        this.screenWantsBlur = wantsBlur;
    }

    public void uploadSharedUniforms() {
        MinecraftClient mc = MinecraftClient.getInstance();
        int outW = mc.getFramebuffer().textureWidth;
        int outH = mc.getFramebuffer().textureHeight;

        try (var map = RenderSystem.getDevice().createCommandEncoder().mapBuffer(samplerInfo, false, true)) {
            Std140Builder b = Std140Builder.intoBuffer(map.data());
            b.putVec2((float) outW, (float) outH);
            b.putVec2((float) outW, (float) outH);
        }

        double[] mx = new double[1];
        double[] my = new double[1];
        GLFW.glfwGetCursorPos(mc.getWindow().getHandle(), mx, my);
        float scale = (float) mc.getWindow().getScaleFactor();
        int fbH = mc.getFramebuffer().textureHeight;

        float time = mc.getRenderTickCounter().getDynamicDeltaTicks();

        try (var map = RenderSystem.getDevice().createCommandEncoder().mapBuffer(customUniforms, false, true)) {
            Std140Builder b = Std140Builder.intoBuffer(map.data());
            b.putFloat(time);
            b.align(16);
            float x = (float) (mx[0] * scale);
            float y = fbH - (float) (my[0] * scale);
            b.putVec4(new Vector4f(x, y, 0f, 0f));
            b.putFloat(this.screenWantsBlur ? 1.0f : 0.0f);
        }
    }

    public void tryApplyBlur(DrawContext context) {
        GuiRenderState state = context.state;
        int blurLayer = ((GuiRenderStateAccessor)state).getBlurLayer();
        if (blurLayer == Integer.MAX_VALUE) {
            state.applyBlur();
        }
    }

    public void addWidget(float x, float y, float w, float h, float radiusPx, Text text, int color, boolean shadow, @Nullable IconInfo iconInfo) {
        if (widgets.size() >= MAX_WIDGETS) return;
        widgets.add(new Widget(x, y, w, h, radiusPx, text, color, shadow, iconInfo));
    }

    public void uploadWidgetInfo() {
        MinecraftClient mc = MinecraftClient.getInstance();
        int fbH = mc.getFramebuffer().textureHeight;
        float scale = (float) mc.getWindow().getScaleFactor();

        try (var map = RenderSystem.getDevice().createCommandEncoder().mapBuffer(widgetInfo, false, true)) {
            Std140Builder b = Std140Builder.intoBuffer(map.data());
            b.putFloat((float) widgets.size());
            b.putFloat(0f).putFloat(0f).putFloat(0f);

            for (int i = 0; i < MAX_WIDGETS; i++) {
                if (i < widgets.size()) {
                    Widget widget = widgets.get(i);
                    float pixelX = widget.x() * scale;
                    float pixelYTop = widget.y() * scale;
                    float pixelW = widget.w() * scale;
                    float pixelH = widget.h() * scale;
                    float centerX = pixelX + 0.5f * pixelW;
                    float centerYTop = pixelYTop + 0.5f * pixelH;
                    float centerYFB = (float) fbH - centerYTop;
                    float rectX = centerX - 0.5f * pixelW;
                    float rectY = centerYFB - 0.5f * pixelH;
                    b.putVec4(rectX, rectY, pixelW, pixelH);
                } else {
                    b.putVec4(0f, 0f, 0f, 0f);
                }
            }
            for (int i = 0; i < MAX_WIDGETS; i++) {
                if (i < widgets.size()) {
                    Widget widget = widgets.get(i);
                    float rad = widget.radiusPx() * scale;
                    b.putVec4(rad, rad, rad, rad);
                } else {
                    b.putVec4(0f, 0f, 0f, 0f);
                }
            }
        }
    }

    public int getCount() {
        return widgets.size();
    }

    public List<Widget> getWidgets() {
        return widgets;
    }

    public GpuBuffer getSamplerInfoBuffer() { return samplerInfo; }
    public GpuBuffer getCustomUniformsBuffer() { return customUniforms; }
    public GpuBuffer getWidgetInfoBuffer() { return widgetInfo; }
}