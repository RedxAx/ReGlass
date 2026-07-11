package restudio.reglass.mixin.client;

//? if >= 26 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.world.item.ItemStack;
//? } else {
/*import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.screen.recipebook.RecipeGroupButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.ItemStack;
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
@Mixin(RecipeBookTabButton.class)
//? } else {
/*@Mixin(RecipeGroupButtonWidget.class)
*///? }
public abstract class RecipeBookTabButtonMixin {
//? if >= 26 {
    @Shadow private RecipeBookComponent.TabInfo tabInfo;
    @Shadow private boolean selected;
//? } else {
    /*@Shadow private RecipeBookWidget.Tab tab;
    @Shadow private boolean groupFocused;
*///? }

//? if >= 26 {
    @Inject(method = "extractContents", at = @At("HEAD"), cancellable = true)
    private void reglass$renderStableTab(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
//? } else {
    /*@Inject(method = "drawIcon", at = @At("HEAD"), cancellable = true)
    private void reglass$renderStableTab(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
*///? }
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            return;
        }

//? if >= 26 {
        RecipeBookTabButton self = (RecipeBookTabButton)(Object) this;
//? } else {
        /*ClickableWidget self = (ClickableWidget)(Object) this;
*///? }
        int x = self.getX();
        int y = self.getY();
        int width = self.getWidth();
        int height = self.getHeight();
        int size = Math.min(width, height) - 3;
        int cx = x + width / 2;
        int cy = y + height / 2;

        ReGlassApi.create(context)
                .dimensions(cx - size / 2, cy - size / 2, size, size)
                .cornerRadius(size * 0.5f)
//? if >= 26 {
                .hover(this.selected ? 1f : 0f)
//? } else {
                /*.hover(this.groupFocused ? 1f : 0f)
*///? }
                .style(WidgetStyle.create()
//? if >= 26 {
                        .tint(0x000000, this.selected ? 0.22f : 0.12f)
                        .layer(this.selected ? 3 : 2))
                .screenSpace()
//? } else {
                        /*.tint(0x000000, this.groupFocused ? 0.22f : 0.12f)
                        .layer(this.groupFocused ? 3 : 2))
*///? }
                .render();

        reglass$renderIcon(context, cx, cy);
        ci.cancel();
    }

    @Unique
//? if >= 26 {
    private void reglass$renderIcon(GuiGraphicsExtractor context, int cx, int cy) {
        if (this.tabInfo.secondaryIcon().isPresent()) {
            context.fakeItem(this.tabInfo.primaryIcon(), cx - 13, cy - 8);
            context.fakeItem((ItemStack)this.tabInfo.secondaryIcon().get(), cx - 3, cy - 8);
//? } else {
    /*private void reglass$renderIcon(DrawContext context, int cx, int cy) {
        if (this.tab.secondaryIcon().isPresent()) {
            context.drawItem(this.tab.primaryIcon(), cx - 13, cy - 8);
            context.drawItem((ItemStack)this.tab.secondaryIcon().get(), cx - 3, cy - 8);
*///? }
            return;
        }
//? if >= 26 {
        context.fakeItem(this.tabInfo.primaryIcon(), cx - 8, cy - 8);
//? } else {
        /*context.drawItem(this.tab.primaryIcon(), cx - 8, cy - 8);
*///? }
    }
}

