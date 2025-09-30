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

    private final MinecraftClient client = MinecraftClient.getInstance();
    private PostEffectProcessor postProcessor;
    private GpuBuffer customUniformsBuffer;
    private boolean loaded = false;
    private float time;
    private final Vector4f mouse = new Vector4f();
    private boolean hasWidgetPointer;
    private final Vector4f widgetPointerLogical = new Vector4f();

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

            List<PostEffectPass> passes = ((PostEffectProcessorAccessor) postProcessor).getPasses();
            for (PostEffectPass pass : passes) {
                Map<String, GpuBuffer> uniformBuffers = ((PostEffectPassAccessor) pass).getUniformBuffers();
                if (uniformBuffers.containsKey("CustomUniforms")) {
                    uniformBuffers.get("CustomUniforms").close();
                    uniformBuffers.put("CustomUniforms", this.customUniformsBuffer);
                }
            }
            loaded = true;
        } catch (Exception e) {
            LiquidGlass.LOGGER.error("LiquidGlassOverlay load failed", e);
            postProcessor = null;
            loaded = false;
        }
    }

    public void registerWidgetPointer(float logicalX, float logicalY, boolean pressed) {
        widgetPointerLogical.x = logicalX;
        widgetPointerLogical.y = logicalY;
        widgetPointerLogical.z = pressed ? 1.0f : 0.0f;
        widgetPointerLogical.w = 0.0f;
        hasWidgetPointer = true;
    }

    private void updateUniforms() {
        time += client.getRenderTickCounter().getDynamicDeltaTicks() / 20f;

        float scale = (float) client.getWindow().getScaleFactor();
        int fbH = client.getFramebuffer().textureHeight;

        if (hasWidgetPointer) {
            float px = widgetPointerLogical.x * scale;
            float py = widgetPointerLogical.y * scale;
            mouse.x = px;
            mouse.y = fbH - py;
            mouse.z = widgetPointerLogical.z;
            mouse.w = 0;
        } else {
            double[] mx = new double[1];
            double[] my = new double[1];
            GLFW.glfwGetCursorPos(client.getWindow().getHandle(), mx, my);
            mouse.x = (float) (mx[0] * scale);
            mouse.y = fbH - (float) (my[0] * scale);
            mouse.z = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS ? 1.0f : 0.0f;
            mouse.w = 0;
        }

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

        hasWidgetPointer = false;
    }

    public void renderAfterGui() {
        if (!loaded || postProcessor == null) return;

        updateUniforms();

        FrameGraphBuilder fgb = new FrameGraphBuilder();
        Framebuffer main = client.getFramebuffer();

        PostEffectProcessor.FramebufferSet framebufferSet = new PostEffectProcessor.FramebufferSet() {
            private Handle<Framebuffer> framebuffer = fgb.createObjectNode("main", main);

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
    }
}