package restudio.reglass.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL32;
import restudio.reglass.mixin.logical.GlGpuBufferAccessor;

import java.util.ArrayList;
import java.util.List;

public final class LiquidGlassUniforms {

    private static final LiquidGlassUniforms INSTANCE = new LiquidGlassUniforms();
    public static LiquidGlassUniforms get() { return INSTANCE; }

    private final GpuBuffer samplerInfo;
    private final GpuBuffer customUniforms;
    private final GpuBuffer widgetInfo;

    private static final int MAX_WIDGETS = 64;
    private final List<float[]> rects = new ArrayList<>();
    private final List<Float> rads = new ArrayList<>();

    private int samplerBinding = -1;
    private int customBinding = -1;
    private int widgetBinding = -1;

    private LiquidGlassUniforms() {
        samplerInfo    = RenderSystem.getDevice().createBuffer(() -> "reglass SamplerInfo", 130, 16);
        customUniforms = RenderSystem.getDevice().createBuffer(() -> "reglass CustomUniforms", 130, 32);
        int widgetInfoSize = 16 * (1 + MAX_WIDGETS + MAX_WIDGETS);
        widgetInfo     = RenderSystem.getDevice().createBuffer(() -> "reglass WidgetInfo", 130, widgetInfoSize);
    }

    public void beginFrame() {
        rects.clear();
        rads.clear();

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
            float x = (float) (mx[0] * scale);
            float y = fbH - (float) (my[0] * scale);
            b.putVec4(new Vector4f(x, y, 0f, 0f));
        }
    }

    public void addRect(float x, float y, float w, float h, float radiusPx) {
        if (rects.size() >= MAX_WIDGETS) return;
        rects.add(new float[]{x, y, w, h});
        rads.add(radiusPx);
    }

    public void uploadWidgetInfo() {
        MinecraftClient mc = MinecraftClient.getInstance();
        int fbH = mc.getFramebuffer().textureHeight;
        float scale = (float) mc.getWindow().getScaleFactor();

        try (var map = RenderSystem.getDevice().createCommandEncoder().mapBuffer(widgetInfo, false, true)) {
            Std140Builder b = Std140Builder.intoBuffer(map.data());
            b.putFloat((float) rects.size());
            b.putFloat(0f).putFloat(0f).putFloat(0f);

            for (int i = 0; i < MAX_WIDGETS; i++) {
                if (i < rects.size()) {
                    float[] r = rects.get(i);
                    float pixelX = r[0] * scale;
                    float pixelYTop = r[1] * scale;
                    float pixelW = r[2] * scale;
                    float pixelH = r[3] * scale;
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
                if (i < rads.size()) {
                    float rad = rads.get(i) * scale;
                    b.putVec4(rad, rad, rad, rad);
                } else {
                    b.putVec4(0f, 0f, 0f, 0f);
                }
            }
        }
    }

    public void bindForCurrentProgram() {
        int program = GL20.glGetInteger(35725);
        if (program == 0) return;

        int siIdx = GL31.glGetUniformBlockIndex(program, "SamplerInfo");
        if (siIdx >= 0) {
            if (samplerBinding < 0) samplerBinding = 2;
            GL31.glUniformBlockBinding(program, siIdx, samplerBinding);
            GL32.glBindBufferRange(35345, samplerBinding, ((GlGpuBufferAccessor) samplerInfo).getId(), 0, samplerInfo.size());
        }

        int cuIdx = GL31.glGetUniformBlockIndex(program, "CustomUniforms");
        if (cuIdx >= 0) {
            if (customBinding < 0) customBinding = 3;
            GL31.glUniformBlockBinding(program, cuIdx, customBinding);
            GL32.glBindBufferRange(35345, customBinding, ((GlGpuBufferAccessor) customUniforms).getId(), 0, customUniforms.size());
        }

        int wiIdx = GL31.glGetUniformBlockIndex(program, "WidgetInfo");
        System.out.println("WidgetInfo idx=" + wiIdx + " program=" + program);
        if (wiIdx >= 0) {
            if (widgetBinding < 0) widgetBinding = 4;
            GL31.glUniformBlockBinding(program, wiIdx, widgetBinding);
            GL32.glBindBufferRange(35345, widgetBinding, ((GlGpuBufferAccessor) widgetInfo).getId(), 0, widgetInfo.size());
        }
    }

    public int getCount() {
        return rects.size();
    }

    public GpuBuffer getSamplerInfoBuffer() { return samplerInfo; }
    public GpuBuffer getCustomUniformsBuffer() { return customUniforms; }
    public GpuBuffer getWidgetInfoBuffer() { return widgetInfo; }
}