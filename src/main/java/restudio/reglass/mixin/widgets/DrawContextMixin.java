package restudio.reglass.mixin.widgets;

import com.mojang.blaze3d.pipeline.RenderPipeline;
//#if MC >= 26
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
//#else
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
//#endif
import java.util.HashMap;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.LiquidGlassUniforms;
import restudio.reglass.client.api.ReGlassApi;
import restudio.reglass.client.api.ReGlassConfig;
import restudio.reglass.client.api.WidgetStyle;

//#if MC >= 26
@Mixin(GuiGraphicsExtractor.class)
//#else
@Mixin(DrawContext.class)
//#endif
public abstract class DrawContextMixin {

    @Unique
//#if MC >= 26
    private static final Identifier BUTTON_TEXTURE = Identifier.withDefaultNamespace("widget/button");
//#else
    private static final Identifier BUTTON_TEXTURE = Identifier.ofVanilla("widget/button");
//#endif
    @Unique
//#if MC >= 26
    private static final Identifier BUTTON_DISABLED_TEXTURE = Identifier.withDefaultNamespace("widget/button_disabled");
//#else
    private static final Identifier BUTTON_DISABLED_TEXTURE = Identifier.ofVanilla("widget/button_disabled");
//#endif
    @Unique
//#if MC >= 26
    private static final Identifier BUTTON_HIGHLIGHTED_TEXTURE = Identifier.withDefaultNamespace("widget/button_highlighted");
//#else
    private static final Identifier BUTTON_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("widget/button_highlighted");
//#endif
    @Unique
//#if MC >= 26
    private static final Identifier VILLAGER_TRADE_ARROW_OUT_OF_STOCK = Identifier.withDefaultNamespace("container/villager/trade_arrow_out_of_stock");
//#else
    private static final Identifier VILLAGER_TRADE_ARROW_OUT_OF_STOCK = Identifier.ofVanilla("container/villager/trade_arrow_out_of_stock");
//#endif
    @Unique
    private static final Map<Long, Double> REGLASS$SCROLLER_Y = new HashMap<>();

//#if MC >= 26
    @Inject(method = "blurBeforeThisStratum", at = @At("HEAD"))
    private void reglass$onBlurBeforeThisStratum(CallbackInfo ci) {
        LiquidGlassUniforms.get().setScreenWantsBlur(true);
    }

    @Inject(method = "blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIII)V",
//#else
    @Inject(method = "drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIIII)V",
//#endif
            at = @At("HEAD"), cancellable = true)
    private void onDrawTexture(RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height, int color, CallbackInfo ci) {
        if (reglass$handleContainerSprite(sprite, x, y, width, height)) {
            ci.cancel();
            return;
        }

        boolean isButtonTexture = sprite.getPath().equals(BUTTON_TEXTURE.getPath())
                || sprite.getPath().equals(BUTTON_DISABLED_TEXTURE.getPath())
                || sprite.getPath().equals(BUTTON_HIGHLIGHTED_TEXTURE.getPath());

//#if MC >= 26
        if (isButtonTexture && (ReGlassConfig.INSTANCE.features.enableRedesign && ReGlassConfig.INSTANCE.features.buttons)) {
//#else
        if (isButtonTexture && ReGlassConfig.INSTANCE.features.enableRedesign && ReGlassConfig.INSTANCE.features.buttons) {
//#endif
            boolean isHighlighted = sprite.getPath().equals(BUTTON_HIGHLIGHTED_TEXTURE.getPath());
            boolean isDisabled = sprite.getPath().equals(BUTTON_DISABLED_TEXTURE.getPath());
            boolean isTradeRowButton = ReGlassConfig.INSTANCE.features.containers && width >= 80 && width <= 100 && height == 20;
            int glassY = isTradeRowButton ? y + 1 : y;
            int glassHeight = isTradeRowButton ? height - 2 : height;
//#if MC >= 26
            ReGlassApi.create((GuiGraphicsExtractor)(Object) this)
//#else
            ReGlassApi.create((DrawContext)(Object) this)
//#endif
                    .position(x, glassY)
                    .size(width, glassHeight)
                    .hover(isHighlighted ? 1f : 0f)
                    .style(WidgetStyle.create().tint(isDisabled ? 0xFF000000 : 0xFFFFFFFF, isDisabled ? 0.4f : 0f))
                    .render();
            ci.cancel();
        }
    }

//#if MC >= 26
    @Inject(method = "blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",
//#else
    @Inject(method = "drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V",
//#endif
            at = @At("HEAD"), cancellable = true)
    private void reglass$onDrawSprite(RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height, CallbackInfo ci) {
        if (reglass$handleContainerSprite(sprite, x, y, width, height)) {
            ci.cancel();
        }
    }

//#if MC >= 26
    @Inject(method = "blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIIIIII)V",
//#else
    @Inject(method = "drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIIIIIII)V",
//#endif
            at = @At("HEAD"), cancellable = true)
    private void reglass$onDrawSlicedSprite(RenderPipeline pipeline, Identifier sprite, int spriteWidth, int spriteHeight, int u, int v, int x, int y, int width, int height, CallbackInfo ci) {
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (cfg.features.enableRedesign && cfg.features.containers && reglass$handleSlicedContainerSprite(sprite, x, y, width, height)) {
            ci.cancel();
        }
    }

//#if MC >= 26
    @Inject(method = "blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIIII)V",
//#else
    @Inject(method = "drawTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIFFIIIII)V",
//#endif
            at = @At("HEAD"), cancellable = true)
//#if MC >= 26
    private void reglass$onBlitTexture(RenderPipeline pipeline, Identifier texture, int x, int y, float u, float v, int width, int height, int sourceWidth, int sourceHeight, int textureWidth, CallbackInfo ci) {
//#else
    private void reglass$onDrawTexture(RenderPipeline pipeline, Identifier texture, int x, int y, float u, float v, int width, int height, int sourceWidth, int sourceHeight, int textureWidth, CallbackInfo ci) {
//#endif
        if (reglass$shouldSkipContainerTexture(texture, width, height)) {
            ci.cancel();
        }
    }

//#if MC >= 26
    @Inject(method = "blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V",
//#else
    @Inject(method = "drawTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIFFIIII)V",
//#endif
            at = @At("HEAD"), cancellable = true)
//#if MC >= 26
    private void reglass$onBlitTextureNoSourceSize(RenderPipeline pipeline, Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, CallbackInfo ci) {
//#else
    private void reglass$onDrawTextureNoSourceSize(RenderPipeline pipeline, Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, CallbackInfo ci) {
//#endif
        if (reglass$handleModMenuButtonTexture(texture, x, y, u, v, width, height, textureWidth, textureHeight)) {
            ci.cancel();
            return;
        }
        if (reglass$shouldSkipContainerTexture(texture, width, height)) {
            ci.cancel();
        }
    }

//#if MC >= 26
    @Inject(method = "blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIIIII)V",
//#else
    @Inject(method = "drawTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIFFIIIIII)V",
//#endif
            at = @At("HEAD"), cancellable = true)
//#if MC >= 26
    private void reglass$onBlitTextureFull(RenderPipeline pipeline, Identifier texture, int x, int y, float u, float v, int width, int height, int sourceWidth, int sourceHeight, int textureWidth, int textureHeight, CallbackInfo ci) {
//#else
    private void reglass$onDrawTextureFull(RenderPipeline pipeline, Identifier texture, int x, int y, float u, float v, int width, int height, int sourceWidth, int sourceHeight, int textureWidth, int textureHeight, CallbackInfo ci) {
//#endif
        if (reglass$handleModMenuButtonTexture(texture, x, y, u, v, width, height, textureWidth, textureHeight)) {
            ci.cancel();
            return;
        }
        if (reglass$shouldSkipContainerTexture(texture, width, height)) {
            ci.cancel();
        }
    }

//#if MC >= 26
    @Inject(method = "blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIIIIII)V",
//#else
    @Inject(method = "drawTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIFFIIIIIII)V",
//#endif
            at = @At("HEAD"), cancellable = true)
//#if MC >= 26
    private void reglass$onBlitTextureFullWithColor(RenderPipeline pipeline, Identifier texture, int x, int y, float u, float v, int width, int height, int sourceWidth, int sourceHeight, int textureWidth, int textureHeight, int color, CallbackInfo ci) {
//#else
    private void reglass$onDrawTextureFullWithColor(RenderPipeline pipeline, Identifier texture, int x, int y, float u, float v, int width, int height, int sourceWidth, int sourceHeight, int textureWidth, int textureHeight, int color, CallbackInfo ci) {
//#endif
        if (reglass$handleModMenuButtonTexture(texture, x, y, u, v, width, height, textureWidth, textureHeight)) {
            ci.cancel();
            return;
        }
        if (reglass$shouldSkipContainerTexture(texture, width, height)) {
            ci.cancel();
        }
    }

    @Unique
    private boolean reglass$handleModMenuButtonTexture(Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
//#if MC >= 26
        if (!cfg.features.enableRedesign || !cfg.features.buttons) {
            return false;
        }
        if (!texture.getNamespace().equals("modmenu")) {
//#else
        if (!cfg.features.enableRedesign || !cfg.features.buttons || !texture.getNamespace().equals("modmenu")) {
//#endif
            return false;
        }

        String path = texture.getPath();
        if (!path.startsWith("textures/gui/") || !path.substring("textures/gui/".length()).contains("button")) {
            return false;
        }

        String fileName = path.substring("textures/gui/".length());
//#if MC >= 26
        Identifier iconTexture = Identifier.fromNamespaceAndPath("reglass", "textures/gui/modmenu/" + fileName);
//#else
        Identifier iconTexture = Identifier.of("reglass", "textures/gui/modmenu/" + fileName);
//#endif
        boolean hovered = v > 0f && v < 40f;
        boolean disabled = v >= 40f;

//#if MC >= 26
        ReGlassApi.create((GuiGraphicsExtractor)(Object) this)
//#else
        ReGlassApi.create((DrawContext)(Object) this)
//#endif
                .dimensions(x, y, width, height)
                .cornerRadius(Math.min(width, height) * 0.5f)
                .hover(hovered ? 1f : 0f)
                .style(WidgetStyle.create()
                        .tint(disabled ? 0x000000 : 0xFFFFFFFF, disabled ? 0.36f : 0f)
                        .layer(3))
//#if MC >= 26
                .screenSpace()
//#endif
                .render();

//#if MC >= 26
        ((GuiGraphicsExtractor)(Object) this).blit(
//#else
        ((DrawContext)(Object) this).drawTexture(
//#endif
                RenderPipelines.GUI_TEXTURED,
                iconTexture,
                x,
                y,
                u,
                v,
                width,
                height,
                textureWidth,
                textureHeight);
        return true;
    }

    @Unique
    private boolean reglass$shouldSkipContainerTexture(Identifier texture, int width, int height) {
//#if MC >= 26
        if (!ReGlassConfig.INSTANCE.features.enableRedesign || !ReGlassConfig.INSTANCE.features.containers) {
            return false;
        }
        if (width < 100 || height < 54) {
//#else
        if (!ReGlassConfig.INSTANCE.features.enableRedesign || !ReGlassConfig.INSTANCE.features.containers || width < 100 || height < 54) {
//#endif
            return false;
        }

        String path = texture.getPath();
        return path.contains("textures/gui/container/")
                || path.contains("textures/gui/recipe_book")
                || path.contains("textures/gui/sprites/container/")
                || path.contains("container/")
                || path.contains("recipe_book");
    }

    @Unique
    private boolean reglass$handleContainerSprite(Identifier sprite, int x, int y, int width, int height) {
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            return false;
        }

        String path = sprite.getPath();
        if (path.equals("recipe_book/button") || path.equals("recipe_book/button_highlighted")) {
            reglass$renderRoundSprite(x, y, width, height, path.contains("highlighted") ? 1f : 0f, 3);
//#if MC >= 26
            ((GuiGraphicsExtractor)(Object) this).fakeItem(new ItemStack(Items.KNOWLEDGE_BOOK), x + width / 2 - 8, y + height / 2 - 8);
//#else
            ((DrawContext)(Object) this).drawItem(new ItemStack(Items.KNOWLEDGE_BOOK), x + width / 2 - 8, y + height / 2 - 8);
//#endif
            return true;
        }

        if (path.equals("container/villager/out_of_stock") || path.equals("container/anvil/error")) {
            reglass$renderTradeArrowOutOfStock(x, y, width, height);
            return true;
        }

        if (path.startsWith("container/loom/pattern")
                || path.startsWith("container/enchanting_table/enchantment_slot")
                || path.startsWith("container/stonecutter/recipe")) {
            return true;
        }

        if (reglass$isScroller(path)) {
            reglass$renderScroller(x, y, width, height, path.contains("disabled"));
            return true;
        }

        if (path.startsWith("container/beacon/button")) {
            return true;
        }

        if (path.equals("container/slot")
                || path.equals("container/horse/chest_slots")
                || path.equals("container/slot/banner")
                || path.equals("container/slot/dye")
                || path.equals("container/slot/banner_pattern")
                || path.equals("container/slot/lapis_lazuli")
                || path.equals("container/slot/brewing_fuel")
                || path.equals("container/slot/potion")) {
            return true;
        }

        if (path.equals("widget/text_field")
                || path.equals("widget/text_field_highlighted")
                || path.equals("container/anvil/text_field")
                || path.equals("container/anvil/text_field_disabled")) {
            if (path.startsWith("container/anvil/text_field")) {
                reglass$renderTextField(x - 56, y, 170, height, path.contains("highlighted"));
            } else {
                reglass$renderTextField(x, y, width, height, path.contains("highlighted"));
            }
            return true;
        }

        if (path.equals("recipe_book/tab") || path.equals("recipe_book/tab_selected")) {
            reglass$renderRoundSprite(x, y, width, height, path.contains("selected") ? 1f : 0f, 3);
            return true;
        }

        if (path.startsWith("recipe_book/filter") || path.startsWith("recipe_book/furnace_filter")) {
            reglass$renderRecipeFilterBackground(x, y, width, height, path.contains("enabled") || path.contains("highlighted") ? 1f : 0f);
            reglass$renderRecipeFilterIcon(path, x, y, width, height);
            return true;
        }

        if (path.startsWith("recipe_book/slot_")) {
            boolean craftable = path.contains("craftable") && !path.contains("uncraftable");
//#if MC >= 26
            boolean many = path.contains("many");
//#endif
            int size = Math.max(16, Math.min(width, height) - 4);
            int cx = x + width / 2;
            int cy = y + height / 2;
//#if MC >= 26
            ReGlassApi.create((GuiGraphicsExtractor)(Object) this)
//#else
            ReGlassApi.create((DrawContext)(Object) this)
//#endif
                    .dimensions(cx - size / 2 - 1, cy - size / 2 - 1, size, size)
                    .cornerRadius(5)
                    .hover(craftable ? 0.65f : 0f)
                    .style(WidgetStyle.create()
                            .tint(0x000000, craftable ? 0.18f : 0.12f)
                            .shadow(0f, 0f, 0f, 0f)
                            .layer(3))
//#if MC >= 26
                    .screenSpace()
//#endif
                    .render();
            return true;
        }

        if (path.startsWith("container/creative_inventory/tab_")) {
            boolean selected = path.contains("_selected_");
            int size = Math.min(24, Math.min(width, height));
            int cx = x + width / 2;
            int cy = y + height / 2;
//#if MC >= 26
            ReGlassApi.create((GuiGraphicsExtractor)(Object) this)
//#else
            ReGlassApi.create((DrawContext)(Object) this)
//#endif
                    .dimensions(cx - size / 2, cy - size / 2, size, size)
                    .cornerRadius(size * 0.5f)
                    .hover(selected ? 1f : 0f)
                    .style(WidgetStyle.create()
                            .tint(0x000000, selected ? 0.22f : 0.12f)
                            .layer(selected ? 3 : 2))
//#if MC >= 26
                    .screenSpace()
//#endif
                    .render();
            return true;
        }

        return false;
    }

    @Unique
    private boolean reglass$handleSlicedContainerSprite(Identifier sprite, int x, int y, int width, int height) {
        String path = sprite.getPath();
        if (path.equals("container/horse/chest_slots")
                || path.equals("container/brewing_stand/fuel_length")
                || path.equals("container/brewing_stand/brew_progress")
                || path.equals("container/brewing_stand/bubbles")) {
            return true;
        }

        if (reglass$isScroller(path)) {
            reglass$renderScroller(x, y, width, height, path.contains("disabled"));
            return true;
        }

        return false;
    }

    @Unique
    private boolean reglass$isScroller(String path) {
        return path.equals("container/villager/scroller")
                || path.equals("container/villager/scroller_disabled")
                || path.equals("container/creative_inventory/scroller")
                || path.equals("container/creative_inventory/scroller_disabled")
                || path.equals("container/loom/scroller")
                || path.equals("container/loom/scroller_disabled")
                || path.equals("container/stonecutter/scroller")
                || path.equals("container/stonecutter/scroller_disabled");
    }

    @Unique
    private void reglass$renderScroller(int x, int y, int width, int height, boolean disabled) {
        if (disabled) {
            return;
        }
        long scrollerKey = reglass$scrollerKey(x, width, height);
        double renderY = reglass$getInterpolatedScrollerY(scrollerKey, y);
        int thumbWidth = Math.max(5, Math.min(width, 8));
        int thumbHeight = Math.max(12, height);
        int cx = x + width / 2;
//#if MC >= 26
        ReGlassApi.create((GuiGraphicsExtractor)(Object) this)
//#else
        ReGlassApi.create((DrawContext)(Object) this)
//#endif
                .dimensions(cx - thumbWidth / 2, (int)Math.round(renderY), thumbWidth, thumbHeight)
                .cornerRadius(thumbWidth * 0.5f)
                .hover(0.7f)
                .style(WidgetStyle.create()
                        .tint(0x000000, 0.18f)
                        .fadeKey(scrollerKey)
                        .layer(4))
//#if MC >= 26
                .screenSpace()
//#endif
                .render();
    }

    @Unique
    private double reglass$getInterpolatedScrollerY(long key, int y) {
        Double current = REGLASS$SCROLLER_Y.get(key);
        if (current == null || Math.abs(current - y) > 80.0) {
            current = (double)y;
        } else {
            double dt = LiquidGlassUniforms.get().getFrameDeltaSeconds();
            double alpha = 1.0 - Math.exp(-Math.max(0.0, dt) / 0.08);
            if (alpha < 0.0) alpha = 0.0;
            if (alpha > 1.0) alpha = 1.0;
            current += (y - current) * alpha;
            if (Math.abs(current - y) < 0.01) {
                current = (double)y;
            }
        }
        if (REGLASS$SCROLLER_Y.size() > 64) {
            REGLASS$SCROLLER_Y.clear();
        }
        REGLASS$SCROLLER_Y.put(key, current);
        return current;
    }

    @Unique
    private long reglass$scrollerKey(int x, int width, int height) {
        long h = 1469598103934665603L;
        h ^= x;
        h *= 1099511628211L;
        h ^= width;
        h *= 1099511628211L;
        h ^= height;
        h *= 1099511628211L;
        return h;
    }

    @Unique
    private void reglass$renderTradeArrowOutOfStock(int x, int y, int width, int height) {
        int arrowWidth = 20;
        int arrowHeight = 18;
//#if MC >= 26
        ((GuiGraphicsExtractor)(Object) this).blitSprite(
//#else
        ((DrawContext)(Object) this).drawGuiTexture(
//#endif
                RenderPipelines.GUI_TEXTURED,
                VILLAGER_TRADE_ARROW_OUT_OF_STOCK,
                x + (width - arrowWidth) / 2,
                y + (height - arrowHeight) / 2,
                arrowWidth,
                arrowHeight);
    }

    @Unique
    private void reglass$renderTextField(int x, int y, int width, int height, boolean highlighted) {
//#if MC >= 26
        ReGlassApi.create((GuiGraphicsExtractor)(Object) this)
//#else
        ReGlassApi.create((DrawContext)(Object) this)
//#endif
                .dimensions(x, y, width, height)
                .cornerRadius(5)
                .hover(highlighted ? 1f : 0f)
                .style(WidgetStyle.create()
                        .tint(0x000000, 0.16f)
                        .layer(3))
//#if MC >= 26
                .screenSpace()
//#endif
                .render();
    }

    @Unique
    private void reglass$renderRecipeFilterIcon(String path, int x, int y, int width, int height) {
        String file = path.substring("recipe_book/".length());
//#if MC >= 26
        Identifier texture = Identifier.fromNamespaceAndPath("reglass", "textures/gui/recipe_book/filter_icons/" + file + ".png");
        ((GuiGraphicsExtractor)(Object) this).blit(
//#else
        Identifier texture = Identifier.of("reglass", "textures/gui/recipe_book/filter_icons/" + file + ".png");
        ((DrawContext)(Object) this).drawTexture(
//#endif
                RenderPipelines.GUI_TEXTURED,
                texture,
                x,
                y,
                0.0f,
                0.0f,
                width,
                height,
                26,
                16,
                26,
                16);
    }

    @Unique
    private void reglass$renderRecipeFilterBackground(int x, int y, int width, int height, float hover) {
//#if MC >= 26
        ReGlassApi.create((GuiGraphicsExtractor)(Object) this)
//#else
        ReGlassApi.create((DrawContext)(Object) this)
//#endif
                .dimensions(x, y, width, height)
                .cornerRadius(5)
                .hover(hover)
                .style(WidgetStyle.create()
                        .tint(0x000000, hover > 0f ? 0.20f : 0.12f)
                        .layer(3))
//#if MC >= 26
                .screenSpace()
//#endif
                .render();
    }

    @Unique
    private void reglass$renderRoundSprite(int x, int y, int width, int height, float hover, int layer) {
        int size = Math.min(width, height);
        if (size > 18) {
            size -= 2;
        }
        int cx = x + width / 2;
        int cy = y + height / 2;
//#if MC >= 26
        ReGlassApi.create((GuiGraphicsExtractor)(Object) this)
//#else
        ReGlassApi.create((DrawContext)(Object) this)
//#endif
                .dimensions(cx - size / 2, cy - size / 2, size, size)
                .cornerRadius(size * 0.5f)
                .hover(hover)
                .style(WidgetStyle.create()
                        .tint(0x000000, hover > 0f ? 0.20f : 0.12f)
                        .layer(layer))
//#if MC >= 26
                .screenSpace()
//#endif
                .render();
    }
}

