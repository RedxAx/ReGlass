package restudio.reglass.mixin.client;

//? if >= 26 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
//? } else {
/*import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
*///? }
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

//? if >= 26 {
    protected TitleScreenMixin(Component title) {
//? } else {
    /*protected TitleScreenMixin(Text title) {
*///? }
        super(title);
    }

//? if >= 26 {
    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void reglass$applyBlurOnTitleScreen(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        this.extractBlurredBackground(context);
//? } else {
    /*@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;render(Lnet/minecraft/client/gui/DrawContext;IIF)V", shift = At.Shift.BY))
    private void reglass$applyBlurOnTitleScreen(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        this.applyBlur(context);
*///? }
    }
}
