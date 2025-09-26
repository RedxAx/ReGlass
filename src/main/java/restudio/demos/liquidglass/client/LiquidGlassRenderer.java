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
import net.minecraft.client.gl.*;
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

public class LiquidGlassRenderer {
    private static final Identifier SHADER_ID = Identifier.of(LiquidGlass.MOD_ID, "liquid_glass");
    private final MinecraftClient client = MinecraftClient.getInstance();
    private PostEffectProcessor postProcessor;
    private GpuBuffer customUniformsBuffer;
    private boolean enabled = false;
    private float time;
    private final Vector4f mouse = new Vector4f();

    public void toggle() {
        this.enabled = !this.enabled;
        if (enabled && postProcessor == null) {
            load();
        } else if (!enabled && postProcessor != null) {
            close();
        }
    }

    private void load() {
        Identifier shaderJsonId = SHADER_ID.withPath("shaders/post/" + SHADER_ID.getPath() + ".json");
        LiquidGlass.LOGGER.info("Manually loading shader definition from: {}", shaderJsonId);

        try {
            Resource resource = client.getResourceManager().getResource(shaderJsonId)
                    .orElseThrow(() -> new IOException("Shader resource file not found: " + shaderJsonId));

            JsonElement jsonElement;
            try (JsonReader reader = new JsonReader(new InputStreamReader(resource.getInputStream()))) {
                jsonElement = JsonParser.parseReader(reader);
            }

            PostEffectPipeline pipeline = PostEffectPipeline.CODEC.parse(JsonOps.INSTANCE, jsonElement)
                    .getOrThrow(error -> new IOException("Failed to parse PostEffectPipeline JSON: " + error));
            LiquidGlass.LOGGER.info("Successfully parsed shader pipeline JSON.");

            ShaderLoader shaderLoader = client.getShaderLoader();
            Field projectionMatrixField = ShaderLoader.class.getDeclaredField("projectionMatrix");
            projectionMatrixField.setAccessible(true);
            ProjectionMatrix2 projectionMatrix = (ProjectionMatrix2) projectionMatrixField.get(shaderLoader);

            Method parseEffectMethod = PostEffectProcessor.class.getDeclaredMethod(
                    "parseEffect", PostEffectPipeline.class, net.minecraft.client.texture.TextureManager.class, Set.class, Identifier.class, ProjectionMatrix2.class
            );
            parseEffectMethod.setAccessible(true);
            LiquidGlass.LOGGER.info("Invoking PostEffectProcessor.parseEffect...");

            this.postProcessor = (PostEffectProcessor) parseEffectMethod.invoke(
                    null, pipeline, client.getTextureManager(), Set.of(PostEffectProcessor.MAIN), SHADER_ID, projectionMatrix
            );

            if (this.postProcessor == null) {
                throw new IllegalStateException("PostEffectProcessor.parseEffect returned null!");
            }

            LiquidGlass.LOGGER.info("SUCCESS: Manually loaded and constructed post effect processor.");

            this.customUniformsBuffer = RenderSystem.getDevice().createBuffer(
                    () -> "liquidglass custom uniforms", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, 32
            );

            List<PostEffectPass> passes = ((PostEffectProcessorAccessor) postProcessor).getPasses();
            for (PostEffectPass pass : passes) {
                Map<String, GpuBuffer> uniformBuffers = ((PostEffectPassAccessor) pass).getUniformBuffers();
                if (uniformBuffers.containsKey("CustomUniforms")) {
                    uniformBuffers.get("CustomUniforms").close();
                    uniformBuffers.put("CustomUniforms", this.customUniformsBuffer);
                }
            }
            LiquidGlass.LOGGER.info("Patched uniform buffers to be writable.");

        } catch (Exception e) {
            LiquidGlass.LOGGER.error("MANUAL SHADER LOAD FAILED. This is the real error:", e);
            this.postProcessor = null;
        }

        if (postProcessor == null) {
            enabled = false;
        }
    }

    private void close() {
        if (postProcessor != null) {
            postProcessor.close();
            postProcessor = null;
        }
        if (customUniformsBuffer != null) {
            customUniformsBuffer.close();
            customUniformsBuffer = null;
        }
    }

    public void tick() {
        if (!enabled || postProcessor == null) {
            return;
        }

        updateUniforms();

        FrameGraphBuilder fgb = new FrameGraphBuilder();
        Framebuffer mainFb = client.getFramebuffer();

        PostEffectProcessor.FramebufferSet framebufferSet = new PostEffectProcessor.FramebufferSet() {
            private Handle<Framebuffer> framebuffer = fgb.createObjectNode("main", mainFb);

            @Override
            public void set(Identifier id, Handle<Framebuffer> handle) {
                if (id.equals(PostEffectProcessor.MAIN)) {
                    this.framebuffer = handle;
                }
            }

            @Nullable
            @Override
            public Handle<Framebuffer> get(Identifier id) {
                if (id.equals(PostEffectProcessor.MAIN)) {
                    return this.framebuffer;
                }
                return null;
            }
        };

        postProcessor.render(fgb, mainFb.textureWidth, mainFb.textureHeight, framebufferSet);
        Pool pool = ((GameRendererAccessor) client.gameRenderer).getPool();
        fgb.run(pool);
    }

    private void updateUniforms() {
        time += client.getRenderTickCounter().getDynamicDeltaTicks() / 20f;

        double[] mouseX = new double[1];
        double[] mouseY = new double[1];
        GLFW.glfwGetCursorPos(client.getWindow().getHandle(), mouseX, mouseY);
        mouse.x = (float) mouseX[0];
        mouse.y = (float) mouseY[0];
        mouse.z = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS ? 1.0f : 0.0f;
        mouse.w = 0;

        if (this.customUniformsBuffer != null) {
            try (GpuBuffer.MappedView view = RenderSystem.getDevice().createCommandEncoder().mapBuffer(this.customUniformsBuffer, false, true)) {
                Std140Builder builder = Std140Builder.intoBuffer(view.data());
                builder.putFloat(time);
                builder.putFloat(0.0f);
                builder.putFloat(0.0f);
                builder.putFloat(0.0f);
                builder.putVec4(mouse.x, mouse.y, mouse.z, mouse.w);
            }
        }
    }
}