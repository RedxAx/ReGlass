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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.BiFunction;

public final class LiquidGlassPrecomputeRuntime {

    private static final LiquidGlassPrecomputeRuntime INSTANCE = new LiquidGlassPrecomputeRuntime();
    public static LiquidGlassPrecomputeRuntime get() { return INSTANCE; }

    private RenderPipeline blurPipeline;
    private RenderPipeline bloomPipeline;

    private GpuTexture blurredTex;
    private GpuTextureView blurredView;
    private GpuTexture bloomTex;
    private GpuTextureView bloomView;

    private GpuBuffer samplerInfoUbo;
    private GpuBuffer blurConfigUboX;
    private GpuBuffer blurConfigUboY;

    // Shader ids we will compile
    private static final Identifier VS_ID     = Identifier.of("reglass", "core/blit_fullscreen");
    private static final Identifier BLUR_ID   = Identifier.of("reglass", "program/blur");
    private static final Identifier BLOOM_ID  = Identifier.of("reglass", "program/bloom");

    private LiquidGlassPrecomputeRuntime() {}

    private static String loadResourceText(Identifier idWithShaderFolderAndExt) {
        try {
            Resource res = MinecraftClient.getInstance().getResourceManager().getResource(idWithShaderFolderAndExt).orElse(null);
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
        if (bloomPipeline == null) {
            bloomPipeline = RenderPipeline.builder()
                    .withLocation(Identifier.of("reglass", "pipeline/bloom"))
                    .withVertexShader(VS_ID)
                    .withFragmentShader(BLOOM_ID)
                    .withUniform("Projection", net.minecraft.client.gl.UniformType.UNIFORM_BUFFER)
                    .withUniform("SamplerInfo", net.minecraft.client.gl.UniformType.UNIFORM_BUFFER)
                    .withSampler("iChannel0Sampler")
                    .withSampler("iChannel1Sampler")
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withDepthWrite(false)
                    .withVertexFormat(VertexFormats.POSITION, VertexFormat.DrawMode.QUADS)
                    .build();
            RenderSystem.getDevice().precompilePipeline(bloomPipeline, sourceGetter);
        }

        if (samplerInfoUbo == null) {
            samplerInfoUbo = RenderSystem.getDevice().createBuffer(() -> "reglass SamplerInfo (pre)", 130, 16);
        }
        if (blurConfigUboX == null) {
            blurConfigUboX = RenderSystem.getDevice().createBuffer(() -> "reglass BlurConfig X", 130, 16);
            try (var map = RenderSystem.getDevice().createCommandEncoder().mapBuffer(blurConfigUboX, false, true)) {
                Std140Builder.intoBuffer(map.data()).putVec2(1f, 0f);
            }
        }
        if (blurConfigUboY == null) {
            blurConfigUboY = RenderSystem.getDevice().createBuffer(() -> "reglass BlurConfig Y", 130, 16);
            try (var map = RenderSystem.getDevice().createCommandEncoder().mapBuffer(blurConfigUboY, false, true)) {
                Std140Builder.intoBuffer(map.data()).putVec2(0f, 1f);
            }
        }
    }

    private void ensureTargets(int w, int h) {
        if (blurredTex == null || blurredTex.getWidth(0) != w || blurredTex.getHeight(0) != h) {
            if (blurredTex != null) {
                if (blurredView != null) blurredView.close();
                blurredTex.close();
            }
            blurredTex = RenderSystem.getDevice().createTexture("reglass blurred", 12, TextureFormat.RGBA8, w, h, 1, 1);
            blurredTex.setTextureFilter(FilterMode.LINEAR, false);
            blurredView = RenderSystem.getDevice().createTextureView(blurredTex);
        }
        if (bloomTex == null || bloomTex.getWidth(0) != w || bloomTex.getHeight(0) != h) {
            if (bloomTex != null) {
                if (bloomView != null) bloomView.close();
                bloomTex.close();
            }
            bloomTex = RenderSystem.getDevice().createTexture("reglass bloom", 12, TextureFormat.RGBA8, w, h, 1, 1);
            bloomTex.setTextureFilter(FilterMode.LINEAR, false);
            bloomView = RenderSystem.getDevice().createTextureView(bloomTex);
        }
    }

    public void run() {
        ensurePipelines();

        var mc = MinecraftClient.getInstance();
        var main = mc.getFramebuffer();
        int w = main.textureWidth;
        int h = main.textureHeight;

        ensureTargets(w, h);

        // Update SamplerInfo once per frame
        try (var map = RenderSystem.getDevice().createCommandEncoder().mapBuffer(samplerInfoUbo, false, true)) {
            Std140Builder.intoBuffer(map.data()).putVec2((float) w, (float) h).putVec2((float) w, (float) h);
        }

        var ce = RenderSystem.getDevice().createCommandEncoder();

        // Utilities
        var quadVB = RenderSystem.getQuadVertexBuffer();
        var idxBufInfo = RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS);
        var ib = idxBufInfo.getIndexBuffer(6);
        var itype = idxBufInfo.getIndexType();

        // Blur X: src main -> dst blurred
        try (RenderPass pass = ce.createRenderPass(() -> "reglass blur X",
                blurredView, java.util.OptionalInt.empty())) {
            pass.setPipeline(blurPipeline);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("SamplerInfo", samplerInfoUbo);
            pass.setUniform("Config", blurConfigUboX);
            pass.bindSampler("DiffuseSampler", main.getColorAttachmentView());
            pass.setVertexBuffer(0, quadVB);
            pass.setIndexBuffer(ib, itype);
            pass.drawIndexed(0, 0, 6, 1);
        }

        // Blur Y: src blurred -> dst blurred (reuse)
        try (RenderPass pass = ce.createRenderPass(() -> "reglass blur Y",
                blurredView, java.util.OptionalInt.empty())) {
            pass.setPipeline(blurPipeline);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("SamplerInfo", samplerInfoUbo);
            pass.setUniform("Config", blurConfigUboY);
            pass.bindSampler("DiffuseSampler", blurredView);
            pass.setVertexBuffer(0, quadVB);
            pass.setIndexBuffer(ib, itype);
            pass.drawIndexed(0, 0, 6, 1);
        }

        // Bloom: src main + blurred -> dst bloom
        try (RenderPass pass = ce.createRenderPass(() -> "reglass bloom",
                bloomView, java.util.OptionalInt.empty())) {
            pass.setPipeline(bloomPipeline);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("SamplerInfo", samplerInfoUbo);
            pass.bindSampler("iChannel0Sampler", main.getColorAttachmentView());
            pass.bindSampler("iChannel1Sampler", blurredView);
            pass.setVertexBuffer(0, quadVB);
            pass.setIndexBuffer(ib, itype);
            pass.drawIndexed(0, 0, 6, 1);
        }
    }

    public GpuTextureView getBlurredView() { return blurredView; }
    public GpuTextureView getBloomView()   { return bloomView; }
}