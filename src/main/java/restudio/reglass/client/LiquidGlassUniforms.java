package restudio.reglass.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.util.math.ColorHelper;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import restudio.reglass.client.api.ReGlassConfig;
import restudio.reglass.client.api.model.Optics;
import restudio.reglass.client.api.model.Smoothing;
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
    private final GpuBuffer bgConfig;

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

        int widgetInfoSize = 16 + MAX_WIDGETS * (16 * 11);
        widgetInfo = RenderSystem.getDevice().createBuffer(() -> "reglass WidgetInfo", 130, widgetInfoSize);

        Std140SizeCalculator bcalc = new Std140SizeCalculator();
        bcalc.putFloat();
        bcalc.putFloat();
        bcalc.putVec2();
        int bgConfigSize = bcalc.get();
        bgConfig = RenderSystem.getDevice().createBuffer(() -> "reglass BgConfig", 130, bgConfigSize);
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

            Vector2f dir2 = config.rimLight.direction();
            b.putVec3(new Vector3f(dir2.x, dir2.y, 0.0f));

            b.align(16);
            b.putVec4(
                    ColorHelper.getRed(config.rimLight.color()) / 255f,
                    ColorHelper.getGreen(config.rimLight.color()) / 255f,
                    ColorHelper.getBlue(config.rimLight.color()) / 255f,
                    config.rimLight.intensity()
            );

            b.putFloat(config.pixelEpsilon);
            b.putFloat(config.debugStep);
        }

        try (var map = RenderSystem.getDevice().createCommandEncoder().mapBuffer(bgConfig, false, true)) {
            Std140Builder b = Std140Builder.intoBuffer(map.data());
            b.putFloat(config.shadowExpand);
            b.putFloat(config.shadowFactor);
            b.putVec2(config.shadowOffsetX * scale, config.shadowOffsetY * scale);
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
                    var w = widgets.get(i);
                    float W = w.x2() - w.x1();
                    float H = w.y2() - w.y1();
                    float px = w.x1() * scale;
                    float pyTop = w.y1() * scale;
                    float pW = W * scale;
                    float pH = H * scale;
                    float cx = px + 0.5f * pW;
                    float cyTop = pyTop + 0.5f * pH;
                    float cyFB = (float) fbH - cyTop;
                    float rectX = cx - 0.5f * pW;
                    float rectY = cyFB - 0.5f * pH;
                    b.putVec4(rectX, rectY, pW, pH);
                } else b.putVec4(0f, 0f, 0f, 0f);
            }

            for (int i = 0; i < MAX_WIDGETS; i++) {
                if (i < widgets.size()) {
                    var w = widgets.get(i);
                    float rad = w.cornerRadius() * scale;
                    b.putVec4(rad, rad, rad, rad);
                } else b.putVec4(0f, 0f, 0f, 0f);
            }

            for (int i = 0; i < MAX_WIDGETS; i++) {
                if (i < widgets.size()) {
                    Tint t = widgets.get(i).style().getTint();
                    b.putVec4(
                            ColorHelper.getRed(t.color()) / 255f,
                            ColorHelper.getGreen(t.color()) / 255f,
                            ColorHelper.getBlue(t.color()) / 255f,
                            t.alpha()
                    );
                } else b.putVec4(0f, 0f, 0f, 0f);
            }

            for (int i = 0; i < MAX_WIDGETS; i++) {
                if (i < widgets.size()) {
                    Optics o = widgets.get(i).style().getOptics();
                    b.putVec4(o.refThickness(), o.refFactor(), o.refDispersion(), o.refFresnelRange());
                } else b.putVec4(0f, 0f, 0f, 0f);
            }
            for (int i = 0; i < MAX_WIDGETS; i++) {
                if (i < widgets.size()) {
                    Optics o = widgets.get(i).style().getOptics();
                    b.putVec4(o.refFresnelHardness(), o.refFresnelFactor(), o.glareRange(), o.glareHardness());
                } else b.putVec4(0f, 0f, 0f, 0f);
            }
            for (int i = 0; i < MAX_WIDGETS; i++) {
                if (i < widgets.size()) {
                    Optics o = widgets.get(i).style().getOptics();
                    b.putVec4(o.glareConvergence(), o.glareOppositeFactor(), o.glareFactor(), o.glareAngleRad());
                } else b.putVec4(0f, 0f, 0f, 0f);
            }

            for (int i = 0; i < MAX_WIDGETS; i++) {
                if (i < widgets.size()) {
                    Smoothing s = widgets.get(i).style().getSmoothing();
                    b.putVec4(s.factor(), 0f, 0f, 0f);
                } else b.putVec4(0f, 0f, 0f, 0f);
            }

            for (int i = 0; i < MAX_WIDGETS; i++) {
                if (i < widgets.size()) {
                    var w = widgets.get(i);
                    ScreenRect sc = w.scissorArea();
                    if (sc != null) {
                        float sL = sc.getLeft() * scale;
                        float sR = sc.getRight() * scale;
                        float sT = sc.getTop() * scale;
                        float sB = sc.getBottom() * scale;
                        b.putVec4(sL, fbH - sB, sR, fbH - sT);
                    } else {
                        b.putVec4(0f, 0f, (float) mc.getFramebuffer().textureWidth, (float) mc.getFramebuffer().textureHeight);
                    }
                } else b.putVec4(0f, 0f, 0f, 0f);
            }
        }
    }

    public int getCount() { return widgets.size(); }

    public GpuBuffer getSamplerInfoBuffer() { return samplerInfo; }
    public GpuBuffer getCustomUniformsBuffer() { return customUniforms; }
    public GpuBuffer getWidgetInfoBuffer() { return widgetInfo; }
    public GpuBuffer getBgConfigBuffer() { return bgConfig; }
}