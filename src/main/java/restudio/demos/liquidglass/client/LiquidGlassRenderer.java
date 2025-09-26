package restudio.demos.liquidglass.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mojang.serialization.JsonOps;
import me.x150.renderer.mixin.GameRendererAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectPipeline;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gl.ShaderLoader;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.render.ProjectionMatrix2;
import net.minecraft.client.util.Handle;
import net.minecraft.client.util.Pool;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import restudio.demos.liquidglass.LiquidGlass;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

public class LiquidGlassRenderer {
    private static final Identifier SHADER_ID = Identifier.of(LiquidGlass.MOD_ID, "passthrough");
    private final MinecraftClient client = MinecraftClient.getInstance();
    private PostEffectProcessor postProcessor;
    private boolean enabled = false;

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
                    .orElseThrow(() -> new IOException("Shader resource file not found!"));

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
                    "parseEffect",
                    PostEffectPipeline.class,
                    net.minecraft.client.texture.TextureManager.class,
                    Set.class,
                    Identifier.class,
                    ProjectionMatrix2.class
            );
            parseEffectMethod.setAccessible(true);
            LiquidGlass.LOGGER.info("Invoking PostEffectProcessor.parseEffect...");

            this.postProcessor = (PostEffectProcessor) parseEffectMethod.invoke(
                    null,
                    pipeline,
                    client.getTextureManager(),
                    Set.of(PostEffectProcessor.MAIN),
                    SHADER_ID,
                    projectionMatrix
            );

            if (this.postProcessor == null) {
                throw new IllegalStateException("PostEffectProcessor.parseEffect returned null!");
            }

            LiquidGlass.LOGGER.info("SUCCESS: Manually loaded and constructed post effect processor.");

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
    }

    public void tick() {
        if (!enabled || postProcessor == null) {
            return;
        }

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
}