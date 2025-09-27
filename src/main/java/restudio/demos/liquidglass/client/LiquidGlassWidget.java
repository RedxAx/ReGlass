package restudio.demos.liquidglass.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.serialization.JsonOps;
import me.x150.renderer.mixin.GameRendererAccessor;
import me.x150.renderer.mixin.PostEffectPassAccessor;
import me.x150.renderer.mixin.PostEffectProcessorAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.render.state.TexturedQuadGuiElementRenderState;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.render.ProjectionMatrix2;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.util.Handle;
import net.minecraft.client.util.Pool;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import restudio.demos.liquidglass.LiquidGlass;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LiquidGlassWidget implements Drawable, AutoCloseable {
    private static final Identifier SHADER_ID = Identifier.of(LiquidGlass.MOD_ID, "liquid_glass");
    private static final Identifier BLUR_X_ID = Identifier.of(LiquidGlass.MOD_ID, "blur_x");
    private static final Identifier BLURRED_ID = Identifier.of(LiquidGlass.MOD_ID, "blurred");
    private static final Identifier BLOOM_ID = Identifier.of(LiquidGlass.MOD_ID, "bloom");
    private static final Identifier FINAL_OUTPUT_ID = Identifier.of(LiquidGlass.MOD_ID, "final_output");

    private final MinecraftClient client = MinecraftClient.getInstance();
    private PostEffectProcessor postProcessor;
    private GpuBuffer customUniformsBuffer;

    private final Map<Identifier, Framebuffer> framebuffers = new HashMap<>();
    private Framebuffer backgroundCapture;

    private final int width;
    private final int height;
    private final int scaledWidth;
    private final int scaledHeight;
    private boolean loaded = false;
    private float time;
    private boolean debugMode = false;

    public LiquidGlassWidget(int width, int height) {
        this.width = width;
        this.height = height;
        float scale = (float) MinecraftClient.getInstance().getWindow().getScaleFactor();
        this.scaledWidth = (int) (width * scale);
        this.scaledHeight = (int) (height * scale);
        load();
    }

    public void toggleDebugMode() {
        this.debugMode = !this.debugMode;
    }

    private void load() {
        try {
            Identifier shaderJsonId = SHADER_ID.withPath("shaders/post/" + SHADER_ID.getPath() + ".json");
            Resource resource = client.getResourceManager().getResource(shaderJsonId).orElseThrow(() -> new IOException("Shader resource file not found!"));
            JsonElement json = JsonParser.parseReader(new InputStreamReader(resource.getInputStream()));
            PostEffectPipeline pipeline = PostEffectPipeline.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(IOException::new);

            backgroundCapture = new SimpleFramebuffer("liquidglass_capture", scaledWidth, scaledHeight, false);

            framebuffers.put(BLUR_X_ID, new SimpleFramebuffer("liquidglass_blur_x", scaledWidth, scaledHeight, false));
            framebuffers.put(BLURRED_ID, new SimpleFramebuffer("liquidglass_blurred", scaledWidth, scaledHeight, false));
            framebuffers.put(BLOOM_ID, new SimpleFramebuffer("liquidglass_bloom", scaledWidth, scaledHeight, false));
            framebuffers.put(FINAL_OUTPUT_ID, new SimpleFramebuffer("liquidglass_final", scaledWidth, scaledHeight, false));

            Set<Identifier> availableTargets = new HashSet<>(framebuffers.keySet());
            availableTargets.add(PostEffectProcessor.MAIN);

            Method parseEffectMethod = PostEffectProcessor.class.getDeclaredMethod("parseEffect", PostEffectPipeline.class, TextureManager.class, Set.class, Identifier.class, ProjectionMatrix2.class);
            parseEffectMethod.setAccessible(true);
            Field projectionMatrixField = ShaderLoader.class.getDeclaredField("projectionMatrix");
            projectionMatrixField.setAccessible(true);
            this.postProcessor = (PostEffectProcessor) parseEffectMethod.invoke(null, pipeline, client.getTextureManager(), availableTargets, SHADER_ID, projectionMatrixField.get(client.getShaderLoader()));

            this.customUniformsBuffer = RenderSystem.getDevice().createBuffer(() -> "liquidglass:customs", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, 32);
            for (PostEffectPass pass : ((PostEffectProcessorAccessor) postProcessor).getPasses()) {
                Map<String, GpuBuffer> uniformBuffers = ((PostEffectPassAccessor) pass).getUniformBuffers();
                if (uniformBuffers.containsKey("CustomUniforms")) {
                    uniformBuffers.get("CustomUniforms").close();
                    uniformBuffers.put("CustomUniforms", this.customUniformsBuffer);
                }
            }
            this.loaded = true;
        } catch (Exception e) {
            LiquidGlass.LOGGER.error("Failed to initialize LiquidGlassWidget", e);
            close();
        }
    }

    private void updateUniforms(int mouseX, int mouseY) {
        time += client.getRenderTickCounter().getDynamicDeltaTicks() / 20f;
        float scale = (float) client.getWindow().getScaleFactor();
        float mouseX_pixel = mouseX * scale;
        float mouseY_pixel = mouseY * scale;

        Vector4f mouse = new Vector4f(
                mouseX_pixel,
                this.scaledHeight - mouseY_pixel,
                GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS ? 1.0f : 0.0f,
                0
        );

        if (this.customUniformsBuffer == null) return;

        try (GpuBuffer.MappedView view = RenderSystem.getDevice().createCommandEncoder().mapBuffer(this.customUniformsBuffer, false, true)) {
            Std140Builder builder = Std140Builder.intoBuffer(view.data());
            builder.putFloat(time);
            builder.putVec4(mouse.x, mouse.y, mouse.z, mouse.w);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!this.loaded) {
            context.fill(0, 0, this.width, this.height, 0x80FF0000);
            return;
        }
        context.drawBorder(0, 0, width, height, 0x40000000);

        Framebuffer clientFb = client.getFramebuffer();
        float scale = (float) client.getWindow().getScaleFactor();

        Matrix3x2f transform = context.getMatrices();
        int sx = (int)(transform.m20 * scale);
        int sy = (int)(clientFb.textureHeight - (transform.m21 * scale + this.scaledHeight));

        GpuTexture sourceTexture = clientFb.getColorAttachment();
        GpuTexture destTexture = backgroundCapture.getColorAttachment();
        if(sourceTexture != null && destTexture != null) {
            RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(
                    sourceTexture, destTexture, 0, 0, 0, sx, sy, this.scaledWidth, this.scaledHeight
            );
        }

        updateUniforms(mouseX, mouseY);

        // Explicitly clear intermediate framebuffers to ensure a clean state
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        for (Framebuffer fb : this.framebuffers.values()) {
            if (fb.getColorAttachment() != null) {
                // Clear to transparent black. The shader should then write opaque pixels.
                encoder.clearColorTexture(fb.getColorAttachment(), 0x00000000);
            }
        }

        FrameGraphBuilder fgb = new FrameGraphBuilder();
        Pool pool = ((GameRendererAccessor)client.gameRenderer).getPool();

        Map<Identifier, Handle<Framebuffer>> graphHandles = new HashMap<>();
        graphHandles.put(PostEffectProcessor.MAIN, fgb.createObjectNode("background_capture", backgroundCapture));
        for(Map.Entry<Identifier, Framebuffer> entry : this.framebuffers.entrySet()) {
            graphHandles.put(entry.getKey(), fgb.createObjectNode(entry.getKey().toString(), entry.getValue()));
        }

        PostEffectProcessor.FramebufferSet fboSet = new PostEffectProcessor.FramebufferSet() {
            @Override
            public void set(Identifier id, Handle<Framebuffer> framebuffer) {
                graphHandles.put(id, framebuffer);
            }

            @Nullable
            @Override
            public Handle<Framebuffer> get(Identifier id) {
                return graphHandles.get(id);
            }
        };

        postProcessor.render(fgb, scaledWidth, scaledHeight, fboSet);
        fgb.run(pool);

        if (debugMode) {
            renderDebugGrid(context);
        } else {
            GpuTextureView finalTexture = framebuffers.get(FINAL_OUTPUT_ID).getColorAttachmentView();
            context.state.addSimpleElement(
                    new TexturedQuadGuiElementRenderState(
                            RenderPipelines.GUI_TEXTURED,
                            TextureSetup.withoutGlTexture(finalTexture),
                            new Matrix3x2f(context.getMatrices()),
                            0, 0, this.width, this.height,
                            0.0f, 1.0f, 1.0f, 0.0f,
                            -1,
                            context.scissorStack.peekLast()
                    )
            );
        }
    }

    private void renderDebugGrid(DrawContext context) {
        float halfW = width / 2f;
        float halfH = height / 2f;

        drawDebugTexture(context, backgroundCapture, "Capture", 0, 0, halfW, halfH);
        drawDebugTexture(context, framebuffers.get(BLUR_X_ID), "Blur X", halfW, 0, halfW, halfH);
        drawDebugTexture(context, framebuffers.get(BLURRED_ID), "Blurred", 0, halfH, halfW, halfH);
        drawDebugTexture(context, framebuffers.get(BLOOM_ID), "Bloom", halfW, halfH, halfW, halfH);
    }

    private void drawDebugTexture(DrawContext context, Framebuffer fb, String label, float x, float y, float w, float h) {
        if (fb == null || fb.getColorAttachmentView() == null) {
            context.fill((int)x, (int)y, (int)(x + w), (int)(y + h), 0x80FF0000);
            context.drawText(client.textRenderer, "NULL", (int)x + 2, (int)y + 2, 0xFFFFFFFF, true);
            return;
        }

        GpuTextureView texture = fb.getColorAttachmentView();
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(x, y);

        context.state.addSimpleElement(
                new TexturedQuadGuiElementRenderState(
                        RenderPipelines.GUI_TEXTURED,
                        TextureSetup.withoutGlTexture(texture),
                        new Matrix3x2f(context.getMatrices()),
                        0, 0, (int)w, (int)h,
                        0.0f, 1.0f, 1.0f, 0.0f, // Flipped V for framebuffer texture
                        -1,
                        context.scissorStack.peekLast()
                )
        );
        context.getMatrices().popMatrix();
        context.drawText(client.textRenderer, label, (int)x + 2, (int)y + 2, 0xFFFFFFFF, true);
    }


    @Override
    public void close() {
        if (postProcessor != null) postProcessor.close();
        if (customUniformsBuffer != null) customUniformsBuffer.close();
        if (backgroundCapture != null) backgroundCapture.delete();
        framebuffers.values().forEach(Framebuffer::delete);
        framebuffers.clear();
        this.loaded = false;
    }
}