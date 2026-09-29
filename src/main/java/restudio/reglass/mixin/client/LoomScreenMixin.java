package restudio.reglass.mixin.client;

//#if MC >= 26
import net.minecraft.client.model.object.banner.BannerFlagModel;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
//#else
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.LoomScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.LoomScreenHandler;
import net.minecraft.text.Text;
//#endif
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
//#if MC >= 26
import org.spongepowered.asm.mixin.injection.Redirect;
//#endif
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.api.ReGlassApi;
import restudio.reglass.client.api.ReGlassConfig;
import restudio.reglass.client.api.WidgetStyle;

@Mixin(LoomScreen.class)
//#if MC >= 26
public abstract class LoomScreenMixin extends AbstractContainerScreen<LoomMenu> {
    protected LoomScreenMixin(LoomMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
//#else
public abstract class LoomScreenMixin extends HandledScreen<LoomScreenHandler> {
    protected LoomScreenMixin(LoomScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
//#endif
    }

//#if MC >= 26
    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void reglass$extractPatternPanel(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
//#else
    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void reglass$extractPatternPanel(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
//#endif
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            return;
        }

        ReGlassApi.create(context)
//#if MC >= 26
                .dimensions(this.leftPos + 58, this.topPos + 12, 58, 58)
//#else
                .dimensions(this.x + 58, this.y + 12, 58, 58)
//#endif
                .cornerRadius(9)
                .style(WidgetStyle.create().tint(0x000000, 0.14f).layer(1))
//#if MC >= 26
                .screenSpace()
//#endif
                .render();
//#if MC >= 26
    }

    @Redirect(
            method = "extractBackground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;bannerPattern(Lnet/minecraft/client/model/object/banner/BannerFlagModel;Lnet/minecraft/world/item/DyeColor;Lnet/minecraft/world/level/block/entity/BannerPatternLayers;IIII)V"
            )
    )
    private void reglass$moveBannerPreview(GuiGraphicsExtractor context, BannerFlagModel model, DyeColor dyeColor, BannerPatternLayers layers, int x1, int y1, int x2, int y2) {
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            context.bannerPattern(model, dyeColor, layers, x1, y1, x2, y2);
            return;
        }

        context.bannerPattern(model, dyeColor, layers, x1 - 3, y1 - 9, x2 - 2, y2 - 9);
//#endif
    }
}

