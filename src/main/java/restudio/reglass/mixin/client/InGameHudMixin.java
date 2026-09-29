package restudio.reglass.mixin.client;

//#if MC >= 26
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
//#if MC >= 26.2
import net.minecraft.client.gui.Hud;
//#else
import net.minecraft.client.gui.Gui;
//#endif
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
//#else
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.GameMode;
//#endif
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.LiquidGlassUniforms;
import restudio.reglass.client.api.ReGlassApi;
import restudio.reglass.client.api.ReGlassConfig;
import restudio.reglass.client.api.WidgetStyle;

//#if MC >= 26.2
@Mixin(Hud.class)
//#elseif MC >= 26
@Mixin(Gui.class)
//#else
@Mixin(InGameHud.class)
//#endif
public abstract class InGameHudMixin {

//#if MC >= 26
    @Shadow @Final private Minecraft minecraft;
//#else
    @Shadow @Final private MinecraftClient client;
//#endif

    @Shadow
//#if MC >= 26
    protected abstract void extractSlot(
            GuiGraphicsExtractor context,
//#else
    protected abstract void renderHotbarItem(
            DrawContext context,
//#endif
            int x,
            int y,
//#if MC >= 26
            DeltaTracker tickCounter,
            Player player,
//#else
            RenderTickCounter tickCounter,
            PlayerEntity player,
//#endif
            ItemStack stack,
            int seed
    );

    @Unique private double reglass$slotBlobX = Double.NaN;
    @Unique private int reglass$lastSelected = -1;

//#if MC >= 26
    @Inject(method = "extractItemHotbar", at = @At("HEAD"), cancellable = true)
//#else
    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
//#endif
    private void reglass$onRenderHotbar(
//#if MC >= 26
            GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci
//#else
            DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci
//#endif
    ) {
        if (!ReGlassConfig.INSTANCE.features.enableRedesign
                || !ReGlassConfig.INSTANCE.features.hotbar) {
            return;
        }
//#if MC >= 26
        if (this.minecraft.gameMode != null && this.minecraft.gameMode.isSpectator()) {
//#else
        if (this.client.interactionManager.getCurrentGameMode() == GameMode.SPECTATOR) {
//#endif
            return;
        }

//#if MC >= 26
        Player player = this.getCameraPlayer();
//#else
        PlayerEntity player = this.getCameraPlayer();
//#endif
        if (player == null) {
            return;
        }

//#if MC >= 26.2
        if (this.minecraft.gui.hud.isHidden()) {
//#elseif MC >= 26
        if (this.minecraft.options.hideGui) {
//#else
        if (this.client.options.hudHidden) {
//#endif
            return;
        }

        ci.cancel();

//#if MC < 26
        Profilers.get().push("reglass-hotbar");

//#endif
        int hotbarWidth = 182;
        int hotbarHeight = 22;
//#if MC >= 26
        int x = this.minecraft.getWindow().getGuiScaledWidth() / 2 - hotbarWidth / 2;
        int offhandY = this.minecraft.getWindow().getGuiScaledHeight() - hotbarHeight;
//#else
        int x = this.client.getWindow().getScaledWidth() / 2 - hotbarWidth / 2;
        int offhandY = this.client.getWindow().getScaledHeight() - hotbarHeight;
//#endif

        ReGlassApi.create(context)
                .dimensions(x, offhandY, hotbarWidth, hotbarHeight)
                .cornerRadius(11)
                .style(new WidgetStyle().tint(0x000000, 0.3f).layer(0))
                .render();

        int selectedSlot = player.getInventory().getSelectedSlot();

        int targetCircleX = x - 1 + selectedSlot * 20;
        if (Double.isNaN(this.reglass$slotBlobX)) {
            this.reglass$slotBlobX = targetCircleX;
        }

//#if MC >= 26
        double deltaTicks = tickCounter.getRealtimeDeltaTicks();
//#else
        double deltaTicks;
        try {
            deltaTicks = tickCounter.getDynamicDeltaTicks();
        } catch (Throwable t) {
            deltaTicks = 1.0 / 60.0 * 20.0;
        }
//#endif
        double deltaSeconds = deltaTicks / 20.0;

        double tau = 0.08;
        double alpha = 1.0 - Math.exp(-deltaSeconds / tau);
        if (alpha < 0.0) alpha = 0.0;
        if (alpha > 1.0) alpha = 1.0;

        this.reglass$slotBlobX += (targetCircleX - this.reglass$slotBlobX) * alpha;

        int circleX = (int) Math.round(this.reglass$slotBlobX) + 1;

        WidgetStyle selectorStyle = new WidgetStyle().smoothing(-0.005f).layer(1);

        ReGlassApi.create(context)
                .dimensions(circleX, offhandY, hotbarHeight, hotbarHeight)
                .cornerRadius(0.5f * hotbarHeight)
                .style(selectorStyle)
                .focus(1f)
                .render();

        for (int i = 0; i < 9; ++i) {
            int itemX = x + 3 + i * 20;
            int itemY = offhandY + 3;
//#if MC >= 26
            this.extractSlot(
//#else
            this.renderHotbarItem(
//#endif
                    context,
                    itemX,
                    itemY,
                    tickCounter,
                    player,
//#if MC >= 26
                    player.getInventory().getItem(i),
//#else
                    player.getInventory().getStack(i),
//#endif
                    i + 1
            );
        }

//#if MC >= 26
        ItemStack offHandStack = player.getOffhandItem();
//#else
        ItemStack offHandStack = player.getOffHandStack();
//#endif
        if (!offHandStack.isEmpty()) {
//#if MC >= 26
            HumanoidArm arm = player.getMainArm().getOpposite();
            int offhandX = (arm == HumanoidArm.LEFT ? x - hotbarHeight - 4 : x + hotbarWidth + 4);
//#else
            Arm arm = player.getMainArm().getOpposite();
            int offhandX = (arm == Arm.LEFT ? x - hotbarHeight - 4 : x + hotbarWidth + 4);
//#endif

            ReGlassApi.create(context)
                    .dimensions(offhandX, offhandY, hotbarHeight, hotbarHeight)
                    .cornerRadius(hotbarHeight * 0.5f)
                    .style(new WidgetStyle().tint(0x000000, 0.3f).layer(0))
                    .render();

//#if MC >= 26
            this.extractSlot(context, offhandX + 3, offhandY + 3, tickCounter, player, offHandStack, 0);
//#else
            this.renderHotbarItem(context, offhandX + 3, offhandY + 3, tickCounter, player, offHandStack, 0);
//#endif
        }

        LiquidGlassUniforms.get().tryApplyBlur(context);
//#if MC < 26

        Profilers.get().pop();
//#endif
    }

    @Shadow
//#if MC >= 26
    private Player getCameraPlayer() {
//#else
    private PlayerEntity getCameraPlayer() {
//#endif
        throw new AssertionError("Mixin application failed!");
    }
}

