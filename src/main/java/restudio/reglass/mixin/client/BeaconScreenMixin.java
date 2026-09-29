package restudio.reglass.mixin.client;

//#if MC >= 26
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
//#else
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ingame.BeaconScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.BeaconScreenHandler;
import net.minecraft.text.Text;
//#endif
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.api.ReGlassApi;
import restudio.reglass.client.api.ReGlassConfig;
import restudio.reglass.client.api.WidgetStyle;

@Mixin(BeaconScreen.class)
//#if MC >= 26
public abstract class BeaconScreenMixin extends AbstractContainerScreen<BeaconMenu> {
    protected BeaconScreenMixin(BeaconMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
//#else
public abstract class BeaconScreenMixin extends HandledScreen<BeaconScreenHandler> {
    protected BeaconScreenMixin(BeaconScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
//#endif
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void reglass$layoutButtons(CallbackInfo ci) {
        reglass$layoutBeaconControls();
    }

//#if MC >= 26
    @Inject(method = "containerTick", at = @At("TAIL"))
//#else
    @Inject(method = "handledScreenTick", at = @At("TAIL"))
//#endif
    private void reglass$relayoutButtons(CallbackInfo ci) {
        reglass$layoutBeaconControls();
    }

    private void reglass$layoutBeaconControls() {
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            return;
        }

        int primaryCols = 3;
//#if MC >= 26
        int primaryX = this.leftPos + 62 - (primaryCols * 28 - 4) / 2;
        int primaryY = this.topPos + 32;
        int secondaryX = this.leftPos + 168 - (2 * 28 - 4) / 2;
        int secondaryY = this.topPos + 46;
//#else
        int primaryX = this.x + 62 - (primaryCols * 28 - 4) / 2;
        int primaryY = this.y + 32;
        int secondaryX = this.x + 168 - (2 * 28 - 4) / 2;
        int secondaryY = this.y + 46;
//#endif

        int powerIndex = 0;
//#if MC >= 26
        for (GuiEventListener child : this.children()) {
            if (!(child instanceof AbstractWidget widget)) {
//#else
        for (Element child : this.children()) {
            if (!(child instanceof ClickableWidget widget)) {
//#endif
                continue;
            }

            String widgetName = widget.getClass().getName();
//#if MC >= 26
            if (widgetName.contains("BeaconConfirmButton")) {
                widget.setX(this.leftPos + 168);
                widget.setY(this.topPos + 104);
            } else if (widgetName.contains("BeaconCancelButton")) {
//#else
            if (widgetName.contains("DoneButtonWidget")) {
                widget.setX(this.x + 168);
                widget.setY(this.y + 104);
            } else if (widgetName.contains("CancelButtonWidget")) {
//#endif
                widget.visible = false;
//#if MC >= 26
            } else if (widgetName.contains("BeaconPowerButton")) {
//#else
            } else if (widgetName.contains("EffectButtonWidget")) {
//#endif
                widget.visible = true;
                if (powerIndex < 5) {
                    widget.setX(primaryX + powerIndex % primaryCols * 28);
                    widget.setY(primaryY + powerIndex / primaryCols * 28);
                } else {
                    int secondary = powerIndex - 5;
                    widget.setX(secondaryX + secondary % 2 * 28);
                    widget.setY(secondaryY);
                }
                powerIndex++;
            }
        }
    }

//#if MC >= 26
    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void reglass$extractPanels(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
//#else
    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void reglass$extractPanels(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
//#endif
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            return;
        }

//#if MC >= 26
        reglass$panel(context, this.leftPos + 20, this.topPos + 28, 84, 58, 0.14f);
        reglass$panel(context, this.leftPos + 140, this.topPos + 42, 56, 30, 0.14f);

//#else
        reglass$panel(context, this.x + 20, this.y + 28, 84, 58, 0.14f);
        reglass$panel(context, this.x + 140, this.y + 42, 56, 30, 0.14f);
//#endif
        reglass$renderPaymentItems(context);
//#if MC >= 26
        reglass$panel(context, this.leftPos + 168, this.topPos + 104, 22, 22, 0.14f);
//#else
        reglass$panel(context, this.x + 168, this.y + 104, 22, 22, 0.14f);
//#endif
    }

    @Redirect(
//#if MC >= 26
            method = "extractBackground",
//#else
            method = "drawBackground",
//#endif
            at = @At(
                    value = "INVOKE",
//#if MC >= 26
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;item(Lnet/minecraft/world/item/ItemStack;II)V"
//#else
                    target = "Lnet/minecraft/client/gui/DrawContext;drawItem(Lnet/minecraft/item/ItemStack;II)V"
//#endif
            )
    )
//#if MC >= 26
    private void reglass$skipVanillaPaymentItems(GuiGraphicsExtractor context, ItemStack itemStack, int x, int y) {
//#else
    private void reglass$skipVanillaPaymentItems(DrawContext context, ItemStack itemStack, int x, int y) {
//#endif
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
//#if MC >= 26
            context.item(itemStack, x, y);
//#else
            context.drawItem(itemStack, x, y);
//#endif
        }
    }

    @Redirect(
//#if MC >= 26
            method = "extractLabels",
//#else
            method = "drawForeground",
//#endif
            at = @At(
                    value = "INVOKE",
//#if MC >= 26
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;centeredText(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"
//#else
                    target = "Lnet/minecraft/client/gui/DrawContext;drawCenteredTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)V"
//#endif
            )
    )
//#if MC >= 26
    private void reglass$centeredText(GuiGraphicsExtractor context, Font font, Component text, int centerX, int y, int color) {
//#else
    private void reglass$centeredText(DrawContext context, TextRenderer font, Text text, int centerX, int y, int color) {
//#endif
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
//#if MC >= 26
            context.centeredText(font, text, centerX, y, color);
//#else
            context.drawCenteredTextWithShadow(font, text, centerX, y, color);
//#endif
            return;
        }

//#if MC >= 26
        context.text(font, text, centerX - font.width(text) / 2, y - 2, 0xFFFFFFFF, true);
//#else
        context.drawText(font, text, centerX - font.getWidth(text) / 2, y - 2, 0xFFFFFFFF, true);
//#endif
    }

//#if MC >= 26
    private void reglass$panel(GuiGraphicsExtractor context, int x, int y, int width, int height, float alpha) {
//#else
    private void reglass$panel(DrawContext context, int x, int y, int width, int height, float alpha) {
//#endif
        ReGlassApi.create(context)
                .dimensions(x, y, width, height)
                .cornerRadius(8)
                .style(WidgetStyle.create().tint(0x000000, alpha).layer(1))
//#if MC >= 26
                .screenSpace()
//#endif
                .render();
    }

//#if MC >= 26
    private void reglass$renderPaymentItems(GuiGraphicsExtractor context) {
//#else
    private void reglass$renderPaymentItems(DrawContext context) {
//#endif
        ItemStack[] items = new ItemStack[] {
                new ItemStack(Items.NETHERITE_INGOT),
                new ItemStack(Items.EMERALD),
                new ItemStack(Items.DIAMOND),
                new ItemStack(Items.GOLD_INGOT),
                new ItemStack(Items.IRON_INGOT)
        };

//#if MC >= 26
        int startX = this.leftPos + 14;
        int y = this.topPos + 106;
//#else
        int startX = this.x + 14;
        int itemY = this.y + 106;
//#endif
        for (int i = 0; i < items.length; i++) {
//#if MC >= 26
            int x = startX + i * 23;
            reglass$panel(context, x - 2, y - 2, 20, 20, 0.13f);
            context.item(items[i], x, y);
//#else
            int itemX = startX + i * 23;
            reglass$panel(context, itemX - 2, itemY - 2, 20, 20, 0.13f);
            context.drawItem(items[i], itemX, itemY);
//#endif
        }
    }
}

