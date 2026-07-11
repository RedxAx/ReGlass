package restudio.reglass.mixin.client;

//? if >= 26 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.EnchantmentMenu;
//? } else {
/*import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.text.Text;
*///? }
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.api.ReGlassApi;
import restudio.reglass.client.api.ReGlassConfig;
import restudio.reglass.client.api.WidgetStyle;

@Mixin(EnchantmentScreen.class)
//? if >= 26 {
public abstract class EnchantmentScreenMixin extends AbstractContainerScreen<EnchantmentMenu> {
    protected EnchantmentScreenMixin(EnchantmentMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
//? } else {
/*public abstract class EnchantmentScreenMixin extends HandledScreen<EnchantmentScreenHandler> {
    protected EnchantmentScreenMixin(EnchantmentScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
*///? }
    }

//? if >= 26 {
    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void reglass$extractEnchantingOptionPanel(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
//? } else {
    /*@Inject(method = "drawBackground", at = @At("TAIL"))
    private void reglass$extractEnchantingOptionPanel(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
*///? }
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            return;
        }

        ReGlassApi.create(context)
//? if >= 26 {
                .dimensions(this.leftPos + 58, this.topPos + 12, 112, 62)
//? } else {
                /*.dimensions(this.x + 58, this.y + 12, 112, 62)
*///? }
                .cornerRadius(9)
                .style(WidgetStyle.create().tint(0x000000, 0.14f).layer(1))
//? if >= 26 {
                .screenSpace()
//? }
                .render();
    }
}

