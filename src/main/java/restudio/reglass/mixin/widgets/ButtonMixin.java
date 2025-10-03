package restudio.reglass.mixin.widgets;

import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.state.TextGuiElementRenderState;
import net.minecraft.client.gui.render.state.TexturedQuadGuiElementRenderState;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.LockButtonWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix3x2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.Config;
import restudio.reglass.client.LiquidGlassUniforms;
import restudio.reglass.client.LiquidGlassWidget;
import restudio.reglass.client.api.ReGlassApi;
import restudio.reglass.client.api.WidgetStyle;
import restudio.reglass.client.api.model.Tint;
import restudio.reglass.mixin.accessor.TextIconButtonWidgetAccessor;

@Mixin(ClickableWidget.class)
public abstract class ButtonMixin {
    @Shadow public abstract Text getMessage();
    @Shadow public abstract int getX();
    @Shadow public abstract int getY();
    @Shadow public abstract int getWidth();
    @Shadow public abstract int getHeight();
    @Shadow protected float alpha;
    @Shadow public boolean active;


    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ClickableWidget;renderWidget(Lnet/minecraft/client/gui/DrawContext;IIF)V"), cancellable = true)
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ClickableWidget widget = (ClickableWidget) (Object) this;
        if (Config.redesginMinecraft && widget instanceof PressableWidget && !(widget instanceof LiquidGlassWidget)) {
            ci.cancel();

            ReGlassApi.create(context).fromWidget(widget).style(active ? new WidgetStyle() : ReGlassApi.inactiveStyle).render();

            LiquidGlassUniforms.get().tryApplyBlur(context);

            MinecraftClient client = MinecraftClient.getInstance();
            TextRenderer textRenderer = client.textRenderer;
            int color = this.active ? 0xFFFFFF : 0xA0A0A0;
            int finalColor = color | MathHelper.ceil(this.alpha * 255.0f) << 24;

            Matrix3x2f pose = new Matrix3x2f(context.getMatrices());

            Text message = getMessage();
            int textWidth = textRenderer.getWidth(message);
            int textX = getX() + getWidth() / 2 - textWidth / 2;
            int textY = getY() + (getHeight() - 8) / 2;

            if (widget instanceof TextIconButtonWidget iconButton) {
                TextIconButtonWidgetAccessor accessor = (TextIconButtonWidgetAccessor) iconButton;
                int iconWidth = accessor.getTextureWidth();
                int iconHeight = accessor.getTextureHeight();

                int iconX = (getX() + (getWidth() - iconWidth) / 2) + 1;
                int iconY = (getY() + (getHeight() - iconHeight) / 2) + 1;

                boolean notSquared = iconButton.getHeight() != iconButton.getWidth();
                if (!message.getString().isEmpty() && notSquared) {
                    iconX = getX() + getWidth() / 2 - (iconWidth + 2 + textWidth) / 2;
                    textX = iconX + iconWidth + 2;
                }

                Sprite iconSprite = client.getGuiAtlasManager().getSprite(accessor.getTexture());
                GpuTextureView atlasTexture = client.getTextureManager().getTexture(iconSprite.getAtlasId()).getGlTextureView();

                context.state.addSimpleElement(new TexturedQuadGuiElementRenderState(
                        RenderPipelines.GUI_TEXTURED,
                        TextureSetup.of(atlasTexture),
                        pose,
                        iconX, iconY,
                        iconX + iconWidth, iconY + iconHeight,
                        iconSprite.getMinU(), iconSprite.getMaxU(),
                        iconSprite.getMinV(), iconSprite.getMaxV(),
                        finalColor, null
                ));

                if (!message.getString().isEmpty() && notSquared) {
                    context.state.addText(new TextGuiElementRenderState(textRenderer, message.asOrderedText(), pose, textX, textY, finalColor, 0, true, null));
                }
            } else {
                context.drawCenteredTextWithShadow(textRenderer, message, getX() + getWidth() / 2, textY, finalColor);
            }
        }
    }
}