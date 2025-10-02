package restudio.reglass.mixin.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.state.TextGuiElementRenderState;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix3x2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.Config;
import restudio.reglass.client.LiquidGlassUniforms;
import restudio.reglass.client.gui.LiquidGlassGuiElementRenderState;

@Mixin(SliderWidget.class)
public abstract class SliderWidgetMixin extends ClickableWidget {

    public SliderWidgetMixin(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
    private void onRenderWidget(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (Config.redesginMinecraft) {
            ci.cancel();

            float cornerRadius = 0.5f * this.getHeight();
            context.state.addSpecialElement(new LiquidGlassGuiElementRenderState(
                    this.getX(),
                    this.getY(),
                    this.getX() + this.getWidth(),
                    this.getY() + this.getHeight(),
                    cornerRadius
            ));

            LiquidGlassUniforms.get().tryApplyBlur(context);

            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            int color = this.active ? 0xFFFFFF : 0xA0A0A0;
            int finalColor = color | MathHelper.ceil(this.alpha * 255.0f) << 24;
            Matrix3x2f pose = new Matrix3x2f(context.getMatrices());

            context.state.addText(new TextGuiElementRenderState(
                    textRenderer,
                    this.getMessage().asOrderedText(),
                    pose,
                    this.getX() + this.getWidth() / 2 - textRenderer.getWidth(this.getMessage()) / 2,
                    this.getY() + (this.getHeight() - 8) / 2,
                    finalColor,
                    0, true, null
            ));
        }
    }
}