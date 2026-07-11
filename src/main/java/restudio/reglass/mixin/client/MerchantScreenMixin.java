package restudio.reglass.mixin.client;

//? if >= 26 {
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
//? } else {
/*import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
*///? }
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.api.ReGlassConfig;

@Mixin(MerchantScreen.class)
//? if >= 26 {
public abstract class MerchantScreenMixin extends AbstractContainerScreen<MerchantMenu> {
    @Shadow @Final private static Component TRADES_LABEL;
    @Shadow @Final private static Identifier TRADE_ARROW_SPRITE;
    @Shadow private int shopItem;
//? } else {
/*public abstract class MerchantScreenMixin extends HandledScreen<MerchantScreenHandler> {
    @Shadow @Final private static Text TRADES_TEXT;
    @Shadow @Final private static Identifier TRADE_ARROW_TEXTURE;
    @Shadow private int selectedIndex;
*///? }

//? if >= 26 {
    protected MerchantScreenMixin(MerchantMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
//? } else {
    /*protected MerchantScreenMixin(MerchantScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
*///? }
    }

//? if >= 26 {
    @Inject(method = "extractLabels", at = @At("HEAD"), cancellable = true)
    private void reglass$extractLabels(GuiGraphicsExtractor context, int mouseX, int mouseY, CallbackInfo ci) {
//? } else {
    /*@Inject(method = "drawForeground", at = @At("HEAD"), cancellable = true)
    private void reglass$drawLabels(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
*///? }
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            return;
        }

//? if >= 26 {
        Component titleText = this.title;
        int traderLevel = this.menu.getTraderLevel();
        if (traderLevel > 0 && traderLevel <= 5 && this.menu.showProgressBar()) {
            titleText = Component.translatable(
                    "merchant.title",
                    this.title,
                    Component.translatable("merchant.level." + traderLevel));
        }
//? } else {
        /*Text titleText = this.title;
        int titleWidth = this.textRenderer.getWidth(titleText);
        context.drawText(this.textRenderer, titleText, 49 + this.backgroundWidth / 2 - titleWidth / 2, 4, 0xFFFFFFFF, true);
*///? }

//? if >= 26 {
        int titleWidth = this.font.width(titleText);
        context.text(this.font, titleText, 49 + this.imageWidth / 2 - titleWidth / 2, reglass$labelY(6), 0xFFFFFFFF, true);

        int tradesWidth = this.font.width(TRADES_LABEL);
        context.text(this.font, TRADES_LABEL, 53 - tradesWidth / 2, reglass$labelY(6), 0xFFFFFFFF, true);
//? } else {
        /*int tradesWidth = this.textRenderer.getWidth(TRADES_TEXT);
        context.drawText(this.textRenderer, TRADES_TEXT, 53 - tradesWidth / 2, 4, 0xFFFFFFFF, true);
*///? }
        ci.cancel();
    }

//? if >= 26 {
    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void reglass$extractSelectedTradeArrow(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
//? } else {
    /*@Inject(method = "drawBackground", at = @At("TAIL"))
    private void reglass$drawSelectedTradeArrow(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
*///? }
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            return;
        }

//? if >= 26 {
        MerchantOffers offers = this.menu.getOffers();
        if (offers.isEmpty() || this.shopItem < 0 || this.shopItem >= offers.size()) {
//? } else {
        /*TradeOfferList offers = this.handler.getRecipes();
        if (offers.isEmpty() || this.selectedIndex < 0 || this.selectedIndex >= offers.size()) {
*///? }
            return;
        }

//? if >= 26 {
        MerchantOffer offer = offers.get(this.shopItem);
        if (offer.isOutOfStock()) {
//? } else {
        /*TradeOffer offer = offers.get(this.selectedIndex);
        if (offer.isDisabled()) {
*///? }
            return;
        }

        int arrowWidth = 20;
        int arrowHeight = 18;
//? if >= 26 {
        context.blitSprite(
//? } else {
        /*context.drawGuiTexture(
*///? }
                RenderPipelines.GUI_TEXTURED,
//? if >= 26 {
                TRADE_ARROW_SPRITE,
                this.leftPos + 182 + (28 - arrowWidth) / 2,
                this.topPos + 35 + (21 - arrowHeight) / 2,
//? } else {
                /*TRADE_ARROW_TEXTURE,
                this.x + 182 + (28 - arrowWidth) / 2,
                this.y + 35 + (21 - arrowHeight) / 2,
*///? }
                arrowWidth,
                arrowHeight);
    }
//? if >= 26 {

    @Unique
    private int reglass$labelY(int y) {
        return y - 2;
    }
//? }
}

