package restudio.reglass.mixin.client;

//#if MC >= 26
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.BrewingStandScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.BrewingStandMenu;
//#else
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.BrewingStandScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
//#endif
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.api.ReGlassConfig;

@Mixin(BrewingStandScreen.class)
//#if MC >= 26
public abstract class BrewingStandScreenMixin extends AbstractContainerScreen<BrewingStandMenu> {
    protected BrewingStandScreenMixin(BrewingStandMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
//#else
public abstract class BrewingStandScreenMixin extends HandledScreen<BrewingStandScreenHandler> {
    protected BrewingStandScreenMixin(BrewingStandScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
//#endif
    }

//#if MC >= 26
    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void reglass$extractBrewingMeters(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
//#else
    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void reglass$extractBrewingMeters(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
//#endif
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            return;
        }

//#if MC >= 26
        int brewTicks = Mth.clamp(this.menu.getBrewingTicks(), 0, 400);
//#else
        int brewTicks = MathHelper.clamp(this.handler.getBrewTime(), 0, 400);
//#endif
        float brewProgress = brewTicks == 0 ? 0f : 1.0f - brewTicks / 400.0f;
//#if MC >= 26
        reglass$progressRail(context, this.leftPos + 78, this.topPos + 33, 18, brewProgress);
//#else
        reglass$progressRail(context, this.x + 78, this.y + 33, 18, brewProgress);
//#endif

//#if MC >= 26
        int fuel = Mth.clamp(this.menu.getFuel(), 0, 20);
        reglass$progressRail(context, this.leftPos + 22, this.topPos + 50, 18, fuel / 20.0f);
//#else
        int fuel = MathHelper.clamp(this.handler.getFuel(), 0, 20);
        reglass$progressRail(context, this.x + 22, this.y + 50, 18, fuel / 20.0f);
//#endif
    }

    @Unique
//#if MC >= 26
    private void reglass$progressRail(GuiGraphicsExtractor context, int x, int y, int width, float progress) {
//#else
    private void reglass$progressRail(DrawContext context, int x, int y, int width, float progress) {
//#endif
        context.fill(x, y, x + width, y + 2, 0x66000000);
//#if MC >= 26
        int filled = Mth.clamp(Math.round(width * progress), 0, width);
//#else
        int filled = MathHelper.clamp(Math.round(width * progress), 0, width);
//#endif
        if (filled > 0) {
            context.fill(x, y, x + filled, y + 2, 0xCCFFFFFF);
        }
    }
}

