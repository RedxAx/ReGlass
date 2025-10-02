package restudio.reglass.mixin.widgets;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.Config;
import restudio.reglass.client.LiquidGlassUniforms;

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
        if (Config.redesginMinecraft && widget instanceof PressableWidget && !(widget instanceof TextIconButtonWidget)) {
            float radiusPx = 0.5f * Math.min(widget.getWidth(), widget.getHeight());
            int color = this.active ? 16777215 : 10526880;
            int finalColor = color | MathHelper.ceil(this.alpha * 255.0f) << 24;

            LiquidGlassUniforms.get().addWidget(this.getX(), this.getY(), this.getWidth(), this.getHeight(), radiusPx, this.getMessage(), finalColor, true);

            ci.cancel();
        }
    }
}