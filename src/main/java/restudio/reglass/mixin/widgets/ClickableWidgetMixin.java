package restudio.reglass.mixin.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.sound.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import restudio.reglass.extensions.PressableWidgetExtension;

@Mixin(ClickableWidget.class)
public abstract class ClickableWidgetMixin {
    @Shadow
    public abstract void playDownSound(SoundManager soundManager);

    @Shadow
    public abstract void onClick(double mouseX, double mouseY);

    @Redirect(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ClickableWidget;playDownSound(Lnet/minecraft/client/sound/SoundManager;)V"))
    private void redirectPlayDownSound(ClickableWidget instance, SoundManager soundManager) {
        if ((Object) this instanceof PressableWidget) {
            // Just play nothing.
            return;
        }
        playDownSound(soundManager);
    }

    @Redirect(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ClickableWidget;onClick(DD)V"))
    private void redirectOnClick(ClickableWidget instance, double mouseX, double mouseY) {
        if ((Object) this instanceof PressableWidget) {
            ((PressableWidgetExtension) this).reglass$setIsDragging(true);
            // Just play nothing.
            return;
        }
        onClick(mouseX, mouseY);
    }

    @Inject(method = "mouseReleased", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ClickableWidget;onRelease(DD)V"))
    private void modifyOnRelease(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof PressableWidget pressableWidget) {
            if (pressableWidget.isMouseOver(mouseX, mouseY)) {
                pressableWidget.playDownSound(MinecraftClient.getInstance().getSoundManager());
                pressableWidget.onClick(mouseX, mouseY);
            }
            ((PressableWidgetExtension) pressableWidget).reglass$setIsDragging(false);
        }
    }
}
