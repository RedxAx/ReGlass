package restudio.reglass.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.GameMode;
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

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow @Final private MinecraftClient client;

    @Shadow
    protected abstract void renderHotbarItem(
            DrawContext context,
            int x,
            int y,
            RenderTickCounter tickCounter,
            PlayerEntity player,
            ItemStack stack,
            int seed
    );

    @Unique private double reglass$slotBlobX = Double.NaN;
    @Unique private int reglass$lastSelected = -1;

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void reglass$onRenderHotbar(
            DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci
    ) {
        if (!ReGlassConfig.INSTANCE.features.enableRedesign
                || !ReGlassConfig.INSTANCE.features.hotbar) {
            return;
        }
        if (this.client.interactionManager.getCurrentGameMode() == GameMode.SPECTATOR) {
            return;
        }

        PlayerEntity player = this.getCameraPlayer();
        if (player == null) {
            return;
        }

        if (this.client.options.hudHidden) {
            return;
        }

        ci.cancel();

        Profilers.get().push("reglass-hotbar");

        int hotbarWidth = 182;
        int hotbarHeight = 22;
        int x = context.getScaledWindowWidth() / 2 - hotbarWidth / 2;
        int y = context.getScaledWindowHeight() - hotbarHeight;

        ReGlassApi.create(context)
                .dimensions(x, y, hotbarWidth, hotbarHeight)
                .cornerRadius(11)
                .style(new WidgetStyle().tint(0x000000, 0.3f))
                .render();

        int selectedSlot = player.getInventory().getSelectedSlot();

        int targetCircleX = x - 1 + selectedSlot * 20;
        if (Double.isNaN(this.reglass$slotBlobX)) {
            this.reglass$slotBlobX = targetCircleX;
        }

        double deltaTicks;
        try {
            deltaTicks = tickCounter.getDynamicDeltaTicks();
        } catch (Throwable t) {
            deltaTicks = 1.0 / 60.0 * 20.0;
        }
        double deltaSeconds = deltaTicks / 20.0;

        double tau = 0.08;
        double alpha = 1.0 - Math.exp(-deltaSeconds / tau);
        if (alpha < 0.0) alpha = 0.0;
        if (alpha > 1.0) alpha = 1.0;

        this.reglass$slotBlobX += (targetCircleX - this.reglass$slotBlobX) * alpha;

        int circleX = (int) Math.round(this.reglass$slotBlobX);

        WidgetStyle selectorStyle = new WidgetStyle().smoothing(-0.005f);

        ReGlassApi.create(context)
                .dimensions(circleX, y, hotbarHeight, hotbarHeight)
                .cornerRadius(0.5f * hotbarHeight)
                .style(selectorStyle)
                .render();

        for (int i = 0; i < 9; ++i) {
            int itemX = x + 3 + i * 20;
            int itemY = y + 3;
            this.renderHotbarItem(
                    context,
                    itemX,
                    itemY,
                    tickCounter,
                    player,
                    player.getInventory().getStack(i),
                    i + 1
            );
        }

        LiquidGlassUniforms.get().tryApplyBlur(context);

        Profilers.get().pop();
    }

    @Shadow
    private PlayerEntity getCameraPlayer() {
        throw new AssertionError("Mixin application failed!");
    }
}