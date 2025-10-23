package restudio.reglass.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.OptionalInt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import restudio.reglass.client.api.ReGlassConfig;
import restudio.reglass.client.gui.QuadVertexBufferProvider;
import restudio.reglass.mixin.accessor.GameRendererAccessor;

public final class LiquidGlassPrecomputeRuntime {

    private static final LiquidGlassPrecomputeRuntime INSTANCE = new LiquidGlassPrecomputeRuntime();

    public static LiquidGlassPrecomputeRuntime get() {
        return INSTANCE;
    }

    private RenderPipeline blurPipeline;

    private GpuTexture blurTempTex;
    private GpuTextureView blurTempView;

    private final HashMap<Integer, GpuTexture> blurredByRadius = new HashMap<>();
    private final HashMap<Integer, GpuTextureView> blurredViewByRadius = new HashMap<>();

    private GpuBuffer samplerInfoUbo;
    private GpuBuffer blurConfigUboX;
    private GpuBuffer blurConfigUboY;

    private static final int MAX_RADIUS = 64;

    private List<Integer> requestedRadii = new ArrayList<>();

    private static final Identifier VS_ID = Identifier.of("reglass", "core/blit_fullscreen");
    private static final Identifier BLUR_ID = Identifier.of("reglass", "program/blur");

    private LiquidGlassPrecomputeRuntime() {}

    private void ensurePipelines() {
        if (blurPipeline == null) {
            blurPipeline = RenderPipeline.builder()
                    .withLocation(Identifier.of("reglass", "pipeline/blur"))
                    .withVertexShader(VS_ID)
                    .withFragmentShader(BLUR_ID)
                    .withUniform("Projection", UniformType.UNIFORM_BUFFER)
                    .withUniform("SamplerInfo", UniformType.UNIFORM_BUFFER)
                    .withUniform("Config", UniformType.UNIFORM_BUFFER)
                    .withSampler("DiffuseSampler")
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withDepthWrite(false)
                    .withVertexFormat(VertexFormats.POSITION, VertexFormat.DrawMode.QUADS)
                    .build();
            RenderSystem.getDevice().precompilePipeline(blurPipeline, null);
        }

        if (samplerInfoUbo == null) {
            samplerInfoUbo = RenderSystem.getDevice().createBuffer(() -> "reglass SamplerInfo (pre)", 130, 16);
        }

        int blurConfigSize = 16 + (MAX_RADIUS + 1) * 16;
        if (blurConfigUboX == null) {
            blurConfigUboX = RenderSystem.getDevice().createBuffer(() -> "reglass BlurConfig X", 130, blurConfigSize);
        }
        if (blurConfigUboY == null) {
            blurConfigUboY = RenderSystem.getDevice().createBuffer(() -> "reglass BlurConfig Y", 130, blurConfigSize);
        }
    }

    private void ensureTempTarget(int w, int h) {
        if (blurTempTex == null || blurTempTex.getWidth(0) != w || blurTempTex.getHeight(0) != h) {
            if (blurTempTex != null) {
                if (blurTempView != null) blurTempView.close();
                blurTempTex.close();
            }
            blurTempTex = RenderSystem.getDevice().createTexture("reglass blurTemp", 12, TextureFormat.RGBA8, w, h, 1, 1);
            blurTempTex.setTextureFilter(FilterMode.LINEAR, false);
            blurTempView = RenderSystem.getDevice().createTextureView(blurTempTex);
        }
    }

    private void ensureOutputForRadius(int w, int h, int radius) {
        GpuTexture tex = blurredByRadius.get(radius);
        if (tex == null || tex.getWidth(0) != w || tex.getHeight(0) != h) {
            if (tex != null) {
                GpuTextureView old = blurredViewByRadius.get(radius);
                if (old != null) old.close();
                tex.close();
            }
            GpuTexture newTex = RenderSystem.getDevice().createTexture("reglass blurred r=" + radius, 12, TextureFormat.RGBA8, w, h, 1, 1);
            newTex.setTextureFilter(FilterMode.LINEAR, false);
            GpuTextureView newView = RenderSystem.getDevice().createTextureView(newTex);
            blurredByRadius.put(radius, newTex);
            blurredViewByRadius.put(radius, newView);
        }
    }

    private static float[] gaussian(int radius) {
        radius = Math.max(0, Math.min(radius, MAX_RADIUS));
        float sigma = radius / 3.0f;
        if (radius == 0) return new float[] {1f};
        float[] kernel = new float[radius + 1];
        float sum = 0f;
        for (int i = 0; i <= radius; i++) {
            float w = (float) Math.exp(-0.5 * ((float) i * (float) i) / (sigma * sigma));
            kernel[i] = w;
            sum += (i == 0) ? w : (2f * w);
        }
        for (int i = 0; i <= radius; i++) kernel[i] /= sum;
        return kernel;
    }

    private void uploadBlur(GpuBuffer ubo, float dx, float dy, int radius) {
        radius = Math.max(0, Math.min(radius, MAX_RADIUS));
        float[] weights = gaussian(radius);
        try (var map = RenderSystem.getDevice().createCommandEncoder().mapBuffer(ubo, false, true)) {
            Std140Builder b = Std140Builder.intoBuffer(map.data());
            b.putVec4(dx, dy, (float) radius, 0f);
            for (int i = 0; i <= MAX_RADIUS; i++) {
                float w = (i <= radius) ? weights[i] : 0f;
                b.putFloat(w);
                b.align(16);
            }
        }
    }

    public void setRequestedRadii(List<Integer> ordered) {
        requestedRadii = new ArrayList<>(ordered);
    }

    public void run() {
        ensurePipelines();

        var mc = MinecraftClient.getInstance();
        var main = mc.getFramebuffer();
        int w = main.textureWidth;
        int h = main.textureHeight;

        ensureTempTarget(w, h);

        try (var map = RenderSystem.getDevice().createCommandEncoder().mapBuffer(samplerInfoUbo, false, true)) {
            Std140Builder.intoBuffer(map.data()).putVec2((float) w, (float) h).putVec2((float) w, (float) h);
        }

        var ce = RenderSystem.getDevice().createCommandEncoder();
        GameRenderer gameRenderer = mc.gameRenderer;
        GuiRenderer guiRenderer = ((GameRendererAccessor) gameRenderer).getGuiRenderer();
        var quadVB = ((QuadVertexBufferProvider) guiRenderer).getQuadVertexBuffer();
        var idxInfo = RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS);
        var ib = idxInfo.getIndexBuffer(6);
        var it = idxInfo.getIndexType();

        int max = Math.min(LiquidGlassUniforms.MAX_BLUR_LEVELS, requestedRadii == null ? 0 : requestedRadii.size());
        if (max == 0) {
            int r = ReGlassConfig.INSTANCE.defaultBlurRadius;
            requestedRadii = List.of(r);
            max = 1;
        }

        for (int k = 0; k < max; k++) {
            int radius = requestedRadii.get(k);
            if (radius <= 0) {
                continue;
            }

            ensureOutputForRadius(w, h, radius);

            uploadBlur(blurConfigUboX, 1f, 0f, radius);
            uploadBlur(blurConfigUboY, 0f, 1f, radius);

            try (RenderPass pass = ce.createRenderPass(() -> "reglass blur X r=" + radius, blurTempView, OptionalInt.empty())) {
                pass.setPipeline(blurPipeline);
                RenderSystem.bindDefaultUniforms(pass);
                pass.setUniform("SamplerInfo", samplerInfoUbo);
                pass.setUniform("Config", blurConfigUboX);
                pass.bindSampler("DiffuseSampler", main.getColorAttachmentView());
                pass.setVertexBuffer(0, quadVB);
                pass.setIndexBuffer(ib, it);
                pass.drawIndexed(0, 0, 6, 1);
            }

            try (RenderPass pass = ce.createRenderPass(() -> "reglass blur Y r=" + radius, blurredViewByRadius.get(radius), OptionalInt.empty())) {
                pass.setPipeline(blurPipeline);
                RenderSystem.bindDefaultUniforms(pass);
                pass.setUniform("SamplerInfo", samplerInfoUbo);
                pass.setUniform("Config", blurConfigUboY);
                pass.bindSampler("DiffuseSampler", blurTempView);
                pass.setVertexBuffer(0, quadVB);
                pass.setIndexBuffer(ib, it);
                pass.drawIndexed(0, 0, 6, 1);
            }
        }
    }

    public GpuTextureView getBlurredViewForRadius(int radius) {
        return blurredViewByRadius.get(radius);
    }
}