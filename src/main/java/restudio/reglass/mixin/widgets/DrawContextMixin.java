package restudio.reglass.mixin.widgets;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.api.ReGlassApi;
import restudio.reglass.client.api.ReGlassConfig;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {

    @Unique
    private static final Identifier BUTTON_TEXTURE = Identifier.ofVanilla("widget/button");
    @Unique
    private static final Identifier BUTTON_DISABLED_TEXTURE = Identifier.ofVanilla("widget/button_disabled");
    @Unique
    private static final Identifier BUTTON_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("widget/button_highlighted");

    @Inject(method = "drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIIII)V",
            at = @At("HEAD"), cancellable = true)
    private void onDrawTexture(RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height, int color, CallbackInfo ci) {
        boolean isButtonTexture = sprite.getPath().equals(BUTTON_TEXTURE.getPath())
                || sprite.getPath().equals(BUTTON_DISABLED_TEXTURE.getPath())
                || sprite.getPath().equals(BUTTON_HIGHLIGHTED_TEXTURE.getPath());

        if (isButtonTexture && (ReGlassConfig.INSTANCE.features.enableRedesign && ReGlassConfig.INSTANCE.features.buttons)) {
            ReGlassApi.create((DrawContext)(Object) this).position(x, y).size(width, height).render();
            ci.cancel();
        }
    }
}