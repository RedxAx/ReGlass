package restudio.reglass.mixin.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.state.TextGuiElementRenderState;
import net.minecraft.client.gui.render.state.TexturedQuadGuiElementRenderState;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix3x2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.Config;
import restudio.reglass.client.LiquidGlassUniforms;
import restudio.reglass.client.gui.LiquidGlassGuiElementRenderState;
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
        if (Config.redesginMinecraft && widget instanceof PressableWidget) {
            ci.cancel();

            float cornerRadius = 0.5f * Math.min(widget.getWidth(), widget.getHeight());
            context.state.addSpecialElement(new LiquidGlassGuiElementRenderState(
                    widget.getX(), widget.getY(),
                    widget.getX() + widget.getWidth(), widget.getY() + widget.getHeight(),
                    cornerRadius
            ));

            LiquidGlassUniforms.get().tryApplyBlur(context);

            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
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
                boolean hasText = !message.getString().isEmpty();

                int iconAndTextWidth = hasText ? iconWidth + 3 + textWidth : iconWidth;
                int iconX = getX() + (getWidth() - iconAndTextWidth) / 2;
                int iconY = getY() + (getHeight() - iconHeight) / 2;

                if (hasText) {
                    textX = iconX + iconWidth + 3;
                }

                context.state.addSimpleElement(new TexturedQuadGuiElementRenderState(
                        RenderPipelines.GUI_TEXTURED,
                        TextureSetup.of(MinecraftClient.getInstance().getTextureManager().getTexture(accessor.getTexture()).getGlTextureView()),
                        pose,
                        iconX, iconY,
                        iconX + iconWidth, iconY + iconHeight,
                        0f, 1f, 0f, 1f,
                        finalColor, null
                ));
            }

            if (!message.getString().isEmpty()) {
                context.state.addText(new TextGuiElementRenderState(
                        textRenderer, message.asOrderedText(), pose, textX, textY, finalColor, 0, true, null
                ));
            }
        }
    }
}