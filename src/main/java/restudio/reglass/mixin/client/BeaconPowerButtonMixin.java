package restudio.reglass.mixin.client;

//? if >= 26 {
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.resources.Identifier;
//? } else {
/*import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.BeaconScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
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
import restudio.reglass.mixin.accessor.BeaconScreenButtonAccessor;

//? if >= 26 {
@Mixin(targets = "net.minecraft.client.gui.screens.inventory.BeaconScreen$BeaconPowerButton")
//? } else {
/*@Mixin(targets = "net.minecraft.client.gui.screen.ingame.BeaconScreen$EffectButtonWidget")
*///? }
public abstract class BeaconPowerButtonMixin {
    @Shadow private Identifier sprite;
//? if < 26 {
    /*@Shadow private boolean primary;
    @Shadow private RegistryEntry<StatusEffect> effect;
    @Shadow @Final BeaconScreen field_2811;
*///? }

    @Unique private static double reglass$selectedBlobX = Double.NaN;
    @Unique private static double reglass$selectedBlobY = Double.NaN;

//? if >= 26 {
    @Inject(method = "extractIcon", at = @At("HEAD"), cancellable = true)
    private void reglass$renderInactiveEffectIcon(GuiGraphicsExtractor context, CallbackInfo ci) {
//? } else {
    /*@Inject(method = "renderExtra", at = @At("HEAD"), cancellable = true)
    private void reglass$renderEffectIcon(DrawContext context, CallbackInfo ci) {
*///? }
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        if (!cfg.features.enableRedesign || !cfg.features.containers) {
            return;
        }

//? if >= 26 {
        AbstractWidget widget = (AbstractWidget)(Object) this;
        if (widget.active && ((BeaconScreenButtonAccessor) this).reglass$isSelected()) {
//? } else {
        /*ClickableWidget widget = (ClickableWidget)(Object) this;
        if (widget.active && reglass$isSelected()) {
*///? }
            int targetX = widget.getX() + 1;
            int targetY = widget.getY() + 1;
            if (Double.isNaN(reglass$selectedBlobX)
                    || Double.isNaN(reglass$selectedBlobY)
                    || Math.abs(reglass$selectedBlobX - targetX) > 80
                    || Math.abs(reglass$selectedBlobY - targetY) > 80) {
                reglass$selectedBlobX = targetX;
                reglass$selectedBlobY = targetY;
            }

//? if >= 26 {
            double deltaTicks = Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks();
            double deltaSeconds = deltaTicks / 20.0;
            double tau = 0.08;
            double alpha = 1.0 - Math.exp(-deltaSeconds / tau);
//? } else {
            /*double deltaSeconds = 1.0 / 60.0;
            double alpha = 1.0 - Math.exp(-deltaSeconds / 0.08);
*///? }
            if (alpha < 0.0) alpha = 0.0;
            if (alpha > 1.0) alpha = 1.0;
            reglass$selectedBlobX += (targetX - reglass$selectedBlobX) * alpha;
            reglass$selectedBlobY += (targetY - reglass$selectedBlobY) * alpha;

            ReGlassApi.create(context)
//? if >= 26 {
                    .dimensions((int) Math.round(reglass$selectedBlobX), (int) Math.round(reglass$selectedBlobY), 20, 20)
//? } else {
                    /*.dimensions((int)Math.round(reglass$selectedBlobX), (int)Math.round(reglass$selectedBlobY), 20, 20)
*///? }
                    .cornerRadius(6)
                    .hover(0.9f)
                    .style(WidgetStyle.create().tint(0x000000, 0.16f).layer(2))
//? if >= 26 {
                    .screenSpace()
//? }
                    .render();
        }

        if (widget.active) {
            return;
        }

//? if >= 26 {
        context.blitSprite(RenderPipelines.GUI_TEXTURED, this.sprite, widget.getX() + 2, widget.getY() + 2, 18, 18, 0xFF555555);
//? } else {
        /*context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, this.sprite, widget.getX() + 2, widget.getY() + 2, 18, 18, 0xFF555555);
*///? }
        ci.cancel();
    }

//? if < 26 {
    /*@Unique
    private boolean reglass$isSelected() {
        RegistryEntry<StatusEffect> selected = this.primary
                ? ((BeaconScreenButtonAccessor) this.field_2811).reglass$getPrimaryEffect()
                : ((BeaconScreenButtonAccessor) this.field_2811).reglass$getSecondaryEffect();
        return selected != null && selected.equals(this.effect);
    }
*///? }
}

