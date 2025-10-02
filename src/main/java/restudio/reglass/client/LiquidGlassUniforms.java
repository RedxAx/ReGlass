package restudio.reglass.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.util.math.ColorHelper;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import restudio.reglass.client.api.ReGlassConfig;
import restudio.reglass.client.api.model.Edge;
import restudio.reglass.client.api.model.Reflection;
import restudio.reglass.client.api.model.Refraction;
import restudio.reglass.client.api.model.RimLight;
import restudio.reglass.client.api.model.Tint;
import restudio.reglass.client.gui.LiquidGlassGuiElementRenderState;
import restudio.reglass.mixin.accessor.GuiRenderStateAccessor;

import java.util.ArrayList;
import java.util.List;

public final class LiquidGlassUniforms {

    private static final LiquidGlassUniforms INSTANCE = new LiquidGlassUniforms();
    public static LiquidGlassUniforms get() { return INSTANCE; }

    private final GpuBuffer samplerInfo;
    private final GpuBuffer customUniforms;
    private final GpuBuffer widgetInfo;

    private static final int MAX_WIDGETS = 64;
    private final List<LiquidGlassGuiElementRenderState> widgets = new ArrayList<>();
    private boolean screenWantsBlur = false;

    private LiquidGlassUniforms() {
        samplerInfo = RenderSystem.getDevice().createBuffer(() -> "reglass SamplerInfo", 130, 16);

        Std140SizeCalculator calc = new Std140SizeCalculator();
        calc.putFloat();
        calc.align(16);
        calc.putVec4();
        calc.putFloat();
        calc.align(16);
        calc.putVec3();
        calc.align(16);
        calc.putVec4();
        calc.putFloat();
        calc.putFloat();
        int customUniformsSize = calc.get();

        customUniforms = RenderSystem.getDevice().createBuffer(() -> "reglass CustomUniforms", 130, customUniformsSize);

        int widgetInfoSize = 16 + (MAX_WIDGETS * (16 + 16 + 16 + 16 + 16 + 16 + 16));
        widgetInfo = RenderSystem.getDevice().createBuffer(() -> "reglass WidgetInfo", 130, widgetInfoSize);
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

        float time = (float) GLFW.glfwGetTime();
        ReGlassConfig config = ReGlassConfig.INSTANCE;

        try (var map = RenderSystem.getDevice().createCommandEncoder().mapBuffer(customUniforms, false, true)) {
            Std140Builder b = Std140Builder.intoBuffer(map.data());
            b.putFloat(time);
            b.align(16);
            float x = (float) (mx[0] * scale);
            float y = fbH - (float) (my[0] * scale);
            b.putVec4(new Vector4f(x, y, 0f, 0f));
            b.putFloat(this.screenWantsBlur ? 1.0f : 0.0f);
            b.align(16);

            Vector2f direction2D = config.rimLight.direction();
            b.putVec3(new Vector3f(direction2D.x, direction2D.y, 0.0f));

            b.align(16);
            b.putVec4(
                    ColorHelper.getRed(config.rimLight.color()) / 255f,
                    ColorHelper.getGreen(config.rimLight.color()) / 255f,
                    ColorHelper.getBlue(config.rimLight.color()) / 255f,
                    config.rimLight.intensity()
            );

            b.putFloat(config.fieldSmoothing);
            b.putFloat(config.pixelEpsilon);
        }
    }

    public void tryApplyBlur(DrawContext context) {
        GuiRenderState state = context.state;
        int blurLayer = ((GuiRenderStateAccessor)state).getBlurLayer();
        if (blurLayer == Integer.MAX_VALUE) {
            state.applyBlur();
        }
    }

    public void addWidget(LiquidGlassGuiElementRenderState element) {
        if (widgets.size() >= MAX_WIDGETS) return;
        widgets.add(element);
    }

    public void uploadWidgetInfo() {
        MinecraftClient mc = MinecraftClient.getInstance();
        int fbH = mc.getFramebuffer().textureHeight;
        float scale = (float) mc.getWindow().getScaleFactor();

        try (var map = RenderSystem.getDevice().createCommandEncoder().mapBuffer(widgetInfo, false, true)) {
            Std140Builder b = Std140Builder.intoBuffer(map.data());
            b.putFloat((float) widgets.size());
            b.align(16);

            for (int i = 0; i < MAX_WIDGETS; i++) {
                if (i < widgets.size()) {
                    LiquidGlassGuiElementRenderState widget = widgets.get(i);
                    float w = widget.x2() - widget.x1();
                    float h = widget.y2() - widget.y1();
                    float pixelX = widget.x1() * scale;
                    float pixelYTop = widget.y1() * scale;
                    float pixelW = w * scale;
                    float pixelH = h * scale;
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
                    LiquidGlassGuiElementRenderState widget = widgets.get(i);
                    float rad = widget.cornerRadius() * scale;
                    b.putVec4(rad, rad, rad, rad);
                } else {
                    b.putVec4(0f, 0f, 0f, 0f);
                }
            }

            for (int i = 0; i < MAX_WIDGETS; i++) {
                if (i < widgets.size()) {
                    Tint tint = widgets.get(i).style().getTint();
                    b.putVec4(
                            ColorHelper.getRed(tint.color()) / 255f,
                            ColorHelper.getGreen(tint.color()) / 255f,
                            ColorHelper.getBlue(tint.color()) / 255f,
                            tint.alpha()
                    );
                } else {
                    b.putVec4(0f, 0f, 0f, 0f);
                }
            }

            for (int i = 0; i < MAX_WIDGETS; i++) {
                if (i < widgets.size()) {
                    Refraction refraction = widgets.get(i).style().getRefraction();
                    b.putVec4(refraction.dimension(), refraction.magnitude(), refraction.minDimension(), refraction.minMagnitude());
                } else {
                    b.putVec4(0f, 0f, 0f, 0f);
                }
            }

            for (int i = 0; i < MAX_WIDGETS; i++) {
                if (i < widgets.size()) {
                    Refraction refraction = widgets.get(i).style().getRefraction();
                    float chromaticAberration = widgets.get(i).style().getRefraction().chromaticAberration();
                    b.putVec4(chromaticAberration, refraction.ior().x, refraction.ior().y, refraction.ior().z);
                } else {
                    b.putVec4(0f, 0f, 0f, 0f);
                }
            }

            for (int i = 0; i < MAX_WIDGETS; i++) {
                if (i < widgets.size()) {
                    Edge edge = widgets.get(i).style().getEdge();
                    Reflection reflection = widgets.get(i).style().getReflection();
                    b.putVec4(edge.dimension(), reflection.offsetMin(), reflection.offsetMagnitude(), 0);
                } else {
                    b.putVec4(0f, 0f, 0f, 0f);
                }
            }

            for (int i = 0; i < MAX_WIDGETS; i++) {
                if (i < widgets.size()) {
                    Edge edge = widgets.get(i).style().getEdge();
                    Reflection reflection = widgets.get(i).style().getReflection();
                    b.putVec4(edge.minDimension(), reflection.minOffsetMin(), reflection.minOffsetMagnitude(), 0);
                } else {
                    b.putVec4(0f, 0f, 0f, 0f);
                }
            }
        }
    }

    public int getCount() {
        return widgets.size();
    }

    public List<LiquidGlassGuiElementRenderState> getWidgets() {
        return widgets;
    }

    public GpuBuffer getSamplerInfoBuffer() { return samplerInfo; }
    public GpuBuffer getCustomUniformsBuffer() { return customUniforms; }
    public GpuBuffer getWidgetInfoBuffer() { return widgetInfo; }
}