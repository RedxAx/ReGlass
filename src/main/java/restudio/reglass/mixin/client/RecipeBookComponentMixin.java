package restudio.reglass.mixin.client;

//? if >= 26 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
//? } else {
/*import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
*///? }
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.api.ReGlassApi;
import restudio.reglass.client.api.ReGlassConfig;
import restudio.reglass.client.api.WidgetStyle;

//? if >= 26 {
@Mixin(RecipeBookComponent.class)
//? } else {
/*@Mixin(RecipeBookWidget.class)
*///? }
public abstract class RecipeBookComponentMixin {
//? if >= 26 {
    @Shadow public abstract boolean isVisible();
    @Shadow private int getXOrigin() {
        throw new AssertionError();
    }
    @Shadow private int getYOrigin() {
//? } else {
    /*@Shadow public abstract boolean isOpen();

    @Shadow private int getLeft() {
*///? }
        throw new AssertionError();
    }

//? if >= 26 {
    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void reglass$renderRecipeBookGlass(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
//? } else {
    /*@Shadow private int getTop() {
        throw new AssertionError();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void reglass$renderRecipeBookGlass(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
*///? }
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
//? if >= 26 {
        if (!cfg.features.enableRedesign || !cfg.features.containers || !this.isVisible()) {
//? } else {
        /*if (!cfg.features.enableRedesign || !cfg.features.containers || !this.isOpen()) {
*///? }
            return;
        }

//? if >= 26 {
        int x = this.getXOrigin();
        int y = this.getYOrigin();
//? }
        ReGlassApi.create(context)
//? if >= 26 {
                .dimensions(x, y, 147, 166)
//? } else {
                /*.dimensions(this.getLeft(), this.getTop(), 147, 166)
*///? }
                .cornerRadius(10)
                .style(reglass$recipeBookStyle().layer(1))
//? if >= 26 {
                .screenSpace()
//? }
                .render();
    }

    @Unique
    private WidgetStyle reglass$recipeBookStyle() {
//? if >= 26 {
        return WidgetStyle.create()
                .tint(0x000000, 0.18f);
//? } else {
        /*return WidgetStyle.create().tint(0x000000, 0.18f);
*///? }
    }
}

