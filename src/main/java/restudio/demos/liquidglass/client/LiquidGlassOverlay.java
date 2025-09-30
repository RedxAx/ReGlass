package restudio.demos.liquidglass.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.JsonOps;
import me.x150.renderer.mixin.GameRendererAccessor;
import me.x150.renderer.mixin.PostEffectPassAccessor;
import me.x150.renderer.mixin.PostEffectProcessorAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectPipeline;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.render.ProjectionMatrix2;
import net.minecraft.client.util.Handle;
import net.minecraft.client.util.Pool;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import restudio.demos.liquidglass.LiquidGlass;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LiquidGlassOverlay {
    private static final Identifier SHADER_ID = Identifier.of(LiquidGlass.MOD_ID, "liquid_glass_ingame");
    private static LiquidGlassOverlay INSTANCE;

    public static LiquidGlassOverlay get() {
        if (INSTANCE == null) {
            INSTANCE = new LiquidGlassOverlay();
        }
        return INSTANCE;
    }

    private static final int MAX_WIDGETS = 9999; // What could go wrong?

    private final MinecraftClient client = MinecraftClient.getInstance();
    private PostEffectProcessor postProcessor;
    private GpuBuffer customUniformsBuffer;
    private GpuBuffer widgetInfoBuffer;
    private boolean loaded = false;
    private float time;
    private final Vector4f mouse = new Vector4f();

    private record Rect(float x, float y, float w, float h, float r) {}

    private final List<Rect> registeredRects = new ArrayList<>();

    private LiquidGlassOverlay() {
        load();
    }

    private void load() {
        Identifier shaderJsonId = SHADER_ID.withPath("shaders/post/" + SHADER_ID.getPath() + ".json");

        try {
            Resource resource = client.getResourceManager().getResource(shaderJsonId)
                    .orElseThrow(() -> new IOException("Shader resource file not found: " + shaderJsonId));

            JsonElement jsonElement;
            try (JsonReader reader = new JsonReader(new InputStreamReader(resource.getInputStream()))) {
                jsonElement = JsonParser.parseReader(reader);
            }

            PostEffectPipeline pipeline = PostEffectPipeline.CODEC.parse(JsonOps.INSTANCE, jsonElement)
                    .getOrThrow(error -> new IOException("Failed to parse PostEffectPipeline JSON: " + error));

            var shaderLoader = client.getShaderLoader();
            Field projectionMatrixField = shaderLoader.getClass().getDeclaredField("projectionMatrix");
            projectionMatrixField.setAccessible(true);
            ProjectionMatrix2 projectionMatrix = (ProjectionMatrix2) projectionMatrixField.get(shaderLoader);

            Method parseEffectMethod = PostEffectProcessor.class.getDeclaredMethod(
                    "parseEffect", PostEffectPipeline.class, net.minecraft.client.texture.TextureManager.class,
                    Set.class, Identifier.class, ProjectionMatrix2.class
            );
            parseEffectMethod.setAccessible(true);

            this.postProcessor = (PostEffectProcessor) parseEffectMethod.invoke(
                    null, pipeline, client.getTextureManager(), Set.of(PostEffectProcessor.MAIN), SHADER_ID, projectionMatrix
            );
            if (this.postProcessor == null) {
                throw new IllegalStateException("PostEffectProcessor.parseEffect returned null!");
            }

            this.customUniformsBuffer = RenderSystem.getDevice().createBuffer(
                    () -> "liquidglass overlay uniforms", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, 32
            );

            int widgetInfoSize = 16 * (1 + MAX_WIDGETS + MAX_WIDGETS);
            this.widgetInfoBuffer = RenderSystem.getDevice().createBuffer(
                    () -> "liquidglass widget info", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, widgetInfoSize
            );

            List<PostEffectPass> passes = ((PostEffectProcessorAccessor) postProcessor).getPasses();
            for (PostEffectPass pass : passes) {
                Map<String, GpuBuffer> uniformBuffers = ((PostEffectPassAccessor) pass).getUniformBuffers();
                if (uniformBuffers.containsKey("CustomUniforms")) {
                    uniformBuffers.get("CustomUniforms").close();
                    uniformBuffers.put("CustomUniforms", this.customUniformsBuffer);
                }
                if (uniformBuffers.containsKey("WidgetInfo")) {
                    uniformBuffers.get("WidgetInfo").close();
                    uniformBuffers.put("WidgetInfo", this.widgetInfoBuffer);
                }
            }
            loaded = true;
        } catch (Exception e) {
            LiquidGlass.LOGGER.error("LiquidGlassOverlay load failed", e);
            postProcessor = null;
            loaded = false;
        }
    }

    public void registerWidgetRect(float logicalX, float logicalY, float logicalW, float logicalH, float cornerRadiusPx) {
        registeredRects.add(new Rect(logicalX, logicalY, logicalW, logicalH, cornerRadiusPx));
    }

    private void updateUniforms() {
        time += client.getRenderTickCounter().getDynamicDeltaTicks() / 20f;

        double[] mx = new double[1];
        double[] my = new double[1];
        GLFW.glfwGetCursorPos(client.getWindow().getHandle(), mx, my);

        float scale = (float) client.getWindow().getScaleFactor();
        int fbH = client.getFramebuffer().textureHeight;

        mouse.x = (float) (mx[0] * scale);
        mouse.y = fbH - (float) (my[0] * scale);
        mouse.z = 0.0f;
        mouse.w = 0.0f;

        if (customUniformsBuffer != null) {
            try (GpuBuffer.MappedView view = RenderSystem.getDevice().createCommandEncoder().mapBuffer(customUniformsBuffer, false, true)) {
                Std140Builder b = Std140Builder.intoBuffer(view.data());
                b.putFloat(time);
                b.putFloat(0f);
                b.putFloat(0f);
                b.putFloat(0f);
                b.putVec4(mouse.x, mouse.y, mouse.z, mouse.w);
            }
        }
    }

    private void updateWidgetInfo() {
        if (widgetInfoBuffer == null) return;

        try (GpuBuffer.MappedView view = RenderSystem.getDevice().createCommandEncoder().mapBuffer(widgetInfoBuffer, false, true)) {
            Std140Builder b = Std140Builder.intoBuffer(view.data());

            int count = Math.min(registeredRects.size(), MAX_WIDGETS);
            b.putFloat((float) count);
            b.putFloat(0f).putFloat(0f).putFloat(0f);

            float scale = (float) client.getWindow().getScaleFactor();

            for (int i = 0; i < MAX_WIDGETS; i++) {
                if (i < count) {
                    Rect r = registeredRects.get(i);
                    float x = r.x * scale;
                    float y = r.y * scale;
                    float w = r.w * scale;
                    float h = r.h * scale;
                    b.putVec4(x, y, w, h);
                } else {
                    b.putVec4(0f, 0f, 0f, 0f);
                }
            }
            for (int i = 0; i < MAX_WIDGETS; i++) {
                if (i < count) {
                    Rect r = registeredRects.get(i);
                    float rad = r.r * scale;
                    b.putVec4(rad, rad, rad, rad);
                } else {
                    b.putVec4(0f, 0f, 0f, 0f);
                }
            }
        }
    }

    public void renderAfterGui() {
        if (!loaded || postProcessor == null) return;

        if (registeredRects.isEmpty()) {
            return;
        }

        updateUniforms();
        updateWidgetInfo();

        Framebuffer main = client.getFramebuffer();

        FrameGraphBuilder fgb = new FrameGraphBuilder();
        Handle<Framebuffer> mainHandle = fgb.createObjectNode("main", main);

        PostEffectProcessor.FramebufferSet framebufferSet = new PostEffectProcessor.FramebufferSet() {
            private Handle<Framebuffer> framebuffer = mainHandle;

            @Override
            public void set(Identifier id, Handle<Framebuffer> handle) {
                if (id.equals(PostEffectProcessor.MAIN)) {
                    this.framebuffer = handle;
                }
            }

            @Nullable
            @Override
            public Handle<Framebuffer> get(Identifier id) {
                if (id.equals(PostEffectProcessor.MAIN)) return framebuffer;
                return null;
            }
        };

        postProcessor.render(fgb, main.textureWidth, main.textureHeight, framebufferSet);
        Pool pool = ((GameRendererAccessor) client.gameRenderer).getPool();
        fgb.run(pool);

        registeredRects.clear();
    }
}