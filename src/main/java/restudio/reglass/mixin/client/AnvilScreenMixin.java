package restudio.reglass.mixin.client;

//#if MC >= 26
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.network.chat.Component;
//#else
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
//#endif
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
//#if MC >= 26
import org.spongepowered.asm.mixin.injection.Redirect;
//#endif
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.api.ReGlassConfig;

@Mixin(AnvilScreen.class)
public abstract class AnvilScreenMixin {
//#if MC >= 26
    @Shadow private EditBox name;
//#else
    @Shadow private TextFieldWidget nameField;
//#endif

//#if MC >= 26
    @Inject(method = "subInit", at = @At("TAIL"))
//#else
    @Inject(method = "setup", at = @At("TAIL"))
//#endif
    private void reglass$layoutNameField(CallbackInfo ci) {
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            return;
        }

//#if MC >= 26
        this.name.setX(this.name.getX() - 54);
        this.name.setWidth(160);
//#else
        this.nameField.setX(this.nameField.getX() - 54);
        this.nameField.setWidth(160);
//#endif
    }

//#if MC >= 26
    @Redirect(
            method = "extractLabels",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"
            )
    )
    private void reglass$text(GuiGraphicsExtractor context, Font font, Component text, int x, int y, int color) {
//#else
    @Inject(method = "drawForeground", at = @At("HEAD"), cancellable = true)
    private void reglass$drawForeground(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
//#endif
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
//#if MC >= 26
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            context.text(font, text, x, y, color);
            return;
//#else
        if (cfg.features.enableRedesign && cfg.features.containers) {
            ci.cancel();
//#endif
        }
//#if MC >= 26

        context.text(font, text, x, y, 0xFFFFFFFF, true);
//#endif
    }
}

