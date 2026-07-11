package restudio.reglass.mixin.client;

//? if >= 26 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
//? } else {
/*import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
*///? }
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import restudio.reglass.client.api.ReGlassApi;
import restudio.reglass.client.api.ReGlassConfig;
import restudio.reglass.client.api.WidgetStyle;
import restudio.reglass.mixin.accessor.SlotAccessor;

//? if >= 26 {
@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
    @Shadow private static CreativeModeTab selectedTab;
    @Shadow private EditBox searchBox;
    @Shadow private Slot destroyItemSlot;
//? } else {
/*@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends HandledScreen<CreativeInventoryScreen.CreativeScreenHandler> {
    @Shadow private static ItemGroup selectedTab;
    @Shadow private TextFieldWidget searchBox;
    @Shadow private Slot deleteItemSlot;
*///? }

//? if >= 26 {
    protected CreativeModeInventoryScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
//? } else {
    /*protected CreativeModeInventoryScreenMixin(CreativeInventoryScreen.CreativeScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
*///? }
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void reglass$alignSearchBox(CallbackInfo ci) {
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers || this.searchBox == null) {
            return;
        }

//? if >= 26 {
        this.searchBox.setY(this.topPos + 4);
//? } else {
        /*this.searchBox.setY(this.y + 4);
*///? }
    }

//? if >= 26 {
    @Inject(method = "selectTab", at = @At("TAIL"))
    private void reglass$spaceSurvivalTab(CreativeModeTab tab, CallbackInfo ci) {
//? } else {
    /*@Inject(method = "setSelectedTab", at = @At("TAIL"))
    private void reglass$spaceSurvivalTab(ItemGroup tab, CallbackInfo ci) {
*///? }
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
//? if >= 26 {
        if (!cfg.features.enableRedesign || !cfg.features.containers || selectedTab.getType() != CreativeModeTab.Type.INVENTORY) {
//? } else {
        /*if (!cfg.features.enableRedesign || !cfg.features.containers || selectedTab.getType() != ItemGroup.Type.INVENTORY) {
*///? }
            return;
        }

//? if >= 26 {
        for (int i = 0; i < this.menu.slots.size(); i++) {
            Slot slot = this.menu.slots.get(i);
            if (slot == this.destroyItemSlot) {
//? } else {
        /*for (int i = 0; i < this.handler.slots.size(); i++) {
            Slot slot = this.handler.slots.get(i);
            if (slot == this.deleteItemSlot) {
*///? }
                reglass$setSlotPosition(slot, 181, 120);
            } else if (i >= 5 && i <= 8) {
                int armorIndex = i - 5;
                reglass$setSlotPosition(slot, 54 + armorIndex / 2 * 54, armorIndex % 2 * 27);
            } else if (i == 45) {
                reglass$setSlotPosition(slot, 24, 14);
            } else if (i >= 36 && i <= 44) {
                reglass$setSlotPosition(slot, 9 + (i - 36) * 18, 120);
            }
        }
    }

    @Inject(method = "getTabY", at = @At("HEAD"), cancellable = true)
//? if >= 26 {
    private void reglass$getTabY(CreativeModeTab tab, CallbackInfoReturnable<Integer> cir) {
//? } else {
    /*private void reglass$getTabY(ItemGroup tab, CallbackInfoReturnable<Integer> cir) {
*///? }
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            return;
        }

//? if >= 26 {
        cir.setReturnValue(tab.row() == CreativeModeTab.Row.TOP ? -36 : this.imageHeight + 4);
//? } else {
        /*cir.setReturnValue(tab.getRow() == ItemGroup.Row.TOP ? -36 : this.backgroundHeight + 4);
*///? }
    }

//? if >= 26 {
    @Inject(method = "extractTabButton", at = @At("HEAD"), cancellable = true)
    private void reglass$extractTabButton(GuiGraphicsExtractor context, int mouseX, int mouseY, CreativeModeTab tab, CallbackInfo ci) {
//? } else {
    /*@Inject(method = "renderTabIcon", at = @At("HEAD"), cancellable = true)
    private void reglass$renderTabIcon(DrawContext context, int mouseX, int mouseY, ItemGroup tab, CallbackInfo ci) {
*///? }
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            return;
        }

//? if >= 26 {
        boolean selected = tab == selectedTab;
        boolean top = tab.row() == CreativeModeTab.Row.TOP;
        int x = this.leftPos + reglass$getTabX(tab);
        int y = this.topPos + (top ? -34 : this.imageHeight + 6);
//? } else {
        /*boolean selected = tab == selectedTab;
        boolean top = tab.getRow() == ItemGroup.Row.TOP;
        int x = this.x + reglass$getTabX(tab);
        int y = this.y + (top ? -34 : this.backgroundHeight + 6);
*///? }
        int blobX = x + 1;
        int blobY = y + (top ? 5 : 3);

        ReGlassApi.create(context)
                .dimensions(blobX, blobY, 24, 24)
                .cornerRadius(12)
                .hover(selected ? 1f : 0f)
                .style(WidgetStyle.create()
                        .tint(0x000000, selected ? 0.22f : 0.12f)
                        .layer(selected ? 3 : 2))
//? if >= 26 {
                .screenSpace()
//? }
                .render();

//? if >= 26 {
        context.item(tab.getIconItem(), x + 5, y + (top ? 9 : 7));
//? } else {
        /*context.drawItem(tab.getIcon(), x + 5, y + (top ? 9 : 7));
*///? }
        ci.cancel();
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
        if (selectedTab.showTitle()) {
            context.text(this.font, selectedTab.getDisplayName(), 8, 4, 0xFFFFFFFF, true);
//? } else {
        /*if (selectedTab.shouldRenderName()) {
            context.drawText(this.textRenderer, selectedTab.getDisplayName(), 8, 4, 0xFFFFFFFF, true);
*///? }
        }
        ci.cancel();
    }

    @Unique
    private void reglass$setSlotPosition(Slot slot, int x, int y) {
        ((SlotAccessor) slot).reglass$setX(x);
        ((SlotAccessor) slot).reglass$setY(y);
    }
//? if >= 26 {

    @Unique
    private int reglass$getTabX(CreativeModeTab tab) {
        int column = tab.column();
        if (tab.isAlignedRight()) {
            return this.imageWidth - 27 * (7 - column) + 1;
        }
        return 27 * column;
    }
//? } else {
    /*@Unique
    private int reglass$getTabX(ItemGroup tab) {
        int column = tab.getColumn();
        if (tab.isSpecial()) {
            return this.backgroundWidth - 27 * (7 - column) + 1;
        }
        return 27 * column;
    }
*///? }
}

