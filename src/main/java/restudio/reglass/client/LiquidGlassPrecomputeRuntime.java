package restudio.reglass.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import restudio.reglass.client.api.ReGlassConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.BiFunction;

public final class LiquidGlassPrecomputeRuntime {

    private static final LiquidGlassPrecomputeRuntime INSTANCE = new LiquidGlassPrecomputeRuntime();
    public static LiquidGlassPrecomputeRuntime get() { return INSTANCE; }

    private RenderPipeline blurPipeline;

    private GpuTexture blurTempTex;
    private GpuTextureView blurTempView;

    private GpuTexture blurredTex;
    private GpuTextureView blurredView;

    private GpuBuffer samplerInfoUbo;
    private GpuBuffer blurConfigUboX;
    private GpuBuffer blurConfigUboY;

    private static final int MAX_RADIUS = 64;

    private static final Identifier VS_ID   = Identifier.of("reglass", "core/blit_fullscreen");
    private static final Identifier BLUR_ID = Identifier.of("reglass", "program/blur");

    private LiquidGlassPrecomputeRuntime() {}

    private static String loadResourceText(Identifier id) {
        try {
            Resource res = MinecraftClient.getInstance().getResourceManager().getResource(id).orElse(null);
            if (res == null) return null;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String s;
                while ((s = br.readLine()) != null) sb.append(s).append('\n');
                return sb.toString();
            }
        } catch (Exception e) {
            return null;
        }
    }

    private String getShaderSource(Identifier logicalId, ShaderType type) {
        String ext = type == ShaderType.VERTEX ? ".vsh" : ".fsh";
        Identifier real = Identifier.of(logicalId.getNamespace(), "shaders/" + logicalId.getPath() + ext);
        return loadResourceText(real);
    }

    private final BiFunction<Identifier, ShaderType, String> sourceGetter = this::getShaderSource;

    private void ensurePipelines() {
        if (blurPipeline == null) {
            blurPipeline = RenderPipeline.builder()
                    .withLocation(Identifier.of("reglass", "pipeline/blur"))
                    .withVertexShader(VS_ID)
                    .withFragmentShader(BLUR_ID)
                    .withUniform("Projection", net.minecraft.client.gl.UniformType.UNIFORM_BUFFER)
                    .withUniform("SamplerInfo", net.minecraft.client.gl.UniformType.UNIFORM_BUFFER)
                    .withUniform("Config", net.minecraft.client.gl.UniformType.UNIFORM_BUFFER)
                    .withSampler("DiffuseSampler")
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withDepthWrite(false)
                    .withVertexFormat(VertexFormats.POSITION, VertexFormat.DrawMode.QUADS)
                    .build();
            RenderSystem.getDevice().precompilePipeline(blurPipeline, sourceGetter);
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

    private void ensureTargets(int w, int h) {
        if (blurTempTex == null || blurTempTex.getWidth(0) != w || blurTempTex.getHeight(0) != h) {
            if (blurTempTex != null) {
                if (blurTempView != null) blurTempView.close();
                blurTempTex.close();
            }
            blurTempTex = RenderSystem.getDevice().createTexture("reglass blurTemp", 12, TextureFormat.RGBA8, w, h, 1, 1);
            blurTempTex.setTextureFilter(FilterMode.LINEAR, false);
            blurTempView = RenderSystem.getDevice().createTextureView(blurTempTex);
        }
        if (blurredTex == null || blurredTex.getWidth(0) != w || blurredTex.getHeight(0) != h) {
            if (blurredTex != null) {
                if (blurredView != null) blurredView.close();
                blurredTex.close();
            }
            blurredTex = RenderSystem.getDevice().createTexture("reglass blurred", 12, TextureFormat.RGBA8, w, h, 1, 1);
            blurredTex.setTextureFilter(FilterMode.LINEAR, false);
            blurredView = RenderSystem.getDevice().createTextureView(blurredTex);
        }
    }

    private static float[] gaussian(int radius) {
        radius = Math.max(0, Math.min(radius, MAX_RADIUS));
        float sigma = radius / 3.0f;
        if (radius == 0) return new float[]{1f};
        float[] kernel = new float[radius + 1];
        float sum = 0f;
        for (int i = 0; i <= radius; i++) {
            float d = i;
            float w = (float)Math.exp(-0.5 * (d*d)/(sigma*sigma));
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
            b.putVec4(dx, dy, (float)radius, 0f);
            for (int i = 0; i <= MAX_RADIUS; i++) {
                float w = (i <= radius) ? weights[i] : 0f;
                b.putFloat(w);
                b.align(16);
            }
        }
    }

    public void run() {
        ensurePipelines();

        var mc = MinecraftClient.getInstance();
        var main = mc.getFramebuffer();
        int w = main.textureWidth;
        int h = main.textureHeight;

        ensureTargets(w, h);

        try (var map = RenderSystem.getDevice().createCommandEncoder().mapBuffer(samplerInfoUbo, false, true)) {
            Std140Builder.intoBuffer(map.data()).putVec2((float) w, (float) h).putVec2((float) w, (float) h);
        }

        int radius = Math.max(0, Math.min(ReGlassConfig.INSTANCE.blurRadius, MAX_RADIUS));
        uploadBlur(blurConfigUboX, 1f, 0f, radius);
        uploadBlur(blurConfigUboY, 0f, 1f, radius);

        var ce = RenderSystem.getDevice().createCommandEncoder();
        var quadVB = RenderSystem.getQuadVertexBuffer();
        var idxInfo = RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS);
        var ib = idxInfo.getIndexBuffer(6);
        var it = idxInfo.getIndexType();

        try (RenderPass pass = ce.createRenderPass(() -> "reglass blur X", blurTempView, java.util.OptionalInt.empty())) {
            pass.setPipeline(blurPipeline);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("SamplerInfo", samplerInfoUbo);
            pass.setUniform("Config", blurConfigUboX);
            pass.bindSampler("DiffuseSampler", main.getColorAttachmentView());
            pass.setVertexBuffer(0, quadVB);
            pass.setIndexBuffer(ib, it);
            pass.drawIndexed(0, 0, 6, 1);
        }

        try (RenderPass pass = ce.createRenderPass(() -> "reglass blur Y", blurredView, java.util.OptionalInt.empty())) {
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

    public GpuTextureView getBlurredView() { return blurredView; }
}