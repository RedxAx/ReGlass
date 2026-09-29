package restudio.reglass.mixin.widgets;

//#if MC >= 26
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
//#else
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;
//#endif
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.runtime.PlatformInput;
import restudio.reglass.client.api.ReGlassApi;
import restudio.reglass.client.api.ReGlassConfig;
import restudio.reglass.client.api.WidgetStyle;

//#if MC >= 26
@Mixin(AbstractButton.class)
public abstract class PressableWidgetMixin extends AbstractWidget {
//#else
@Mixin(PressableWidget.class)
public abstract class PressableWidgetMixin extends ClickableWidget {
//#endif
    @Unique
    private int reglass$pressedButton = -1;
    @Unique
    private boolean reglass$doubleClick;
    @Unique
    private Object reglass$pressedScreen;

//#if MC >= 26
    protected PressableWidgetMixin(int x, int y, int width, int height, Component message) {
//#else
    protected PressableWidgetMixin(int x, int y, int width, int height, Text message) {
//#endif
        super(x, y, width, height, message);
    }

    @Unique
    private static boolean reglass$enabled() {
        return ReGlassConfig.INSTANCE.features.enableRedesign && ReGlassConfig.INSTANCE.features.buttons;
    }

    @Unique
    private static Object reglass$currentScreen() {
//#if MC >= 26.2
        return Minecraft.getInstance().gui.screen();
//#elseif MC >= 26
        return Minecraft.getInstance().screen;
//#else
        return MinecraftClient.getInstance().currentScreen;
//#endif
    }

    @Unique
    private void reglass$cancelPress() {
        reglass$pressedButton = -1;
        reglass$pressedScreen = null;
    }

    @Override
//#if MC >= 26
    public boolean mouseClicked(MouseButtonEvent click, boolean isDouble) {
//#else
    public boolean mouseClicked(Click click, boolean isDouble) {
//#endif
        if (!reglass$enabled()) {
            reglass$cancelPress();
            return super.mouseClicked(click, isDouble);
        }
        boolean validButton = isValidClickButton(click.buttonInfo());
        if (!active || !visible || !validButton || !isMouseOver(click.x(), click.y())) {
            return false;
        }
        reglass$pressedButton = click.button();
        reglass$doubleClick = isDouble;
        reglass$pressedScreen = reglass$currentScreen();
        return true;
    }

    @Override
//#if MC >= 26
    public boolean mouseReleased(MouseButtonEvent click) {
//#else
    public boolean mouseReleased(Click click) {
//#endif
        if (reglass$pressedButton == -1) {
            return super.mouseReleased(click);
        }
        if (click.button() != reglass$pressedButton) {
            return false;
        }
        boolean activate = reglass$enabled() && active && visible
                && reglass$pressedScreen == reglass$currentScreen() && isMouseOver(click.x(), click.y());
        boolean isDouble = reglass$doubleClick;
        reglass$cancelPress();
        if (activate) {
//#if MC >= 26
            playDownSound(Minecraft.getInstance().getSoundManager());
//#else
            playDownSound(MinecraftClient.getInstance().getSoundManager());
//#endif
            onClick(click, isDouble);
        }
        return true;
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused) {
            reglass$cancelPress();
        }
    }

    @Unique
    private boolean reglass$isHeld() {
        if (reglass$pressedButton == -1) {
            return false;
        }
        // Clear a press if its release was lost while changing screens or window focus.
        if (!reglass$enabled() || !active || !visible || reglass$pressedScreen != reglass$currentScreen()
                || !PlatformInput.isWindowFocused()
                || !PlatformInput.isMouseButtonDown(reglass$pressedButton)) {
            reglass$cancelPress();
            return false;
        }
        return true;
    }

//#if MC >= 26
    @Inject(method = "extractDefaultSprite", at = @At("HEAD"), cancellable = true)
    private void reglass$renderButton(GuiGraphicsExtractor context, CallbackInfo ci) {
//#else
    @Inject(method = "drawButton", at = @At("HEAD"), cancellable = true)
    private void reglass$renderButton(DrawContext context, CallbackInfo ci) {
//#endif
        boolean held = reglass$isHeld();
        if (!reglass$enabled()) {
            return;
        }
        boolean hovered = active && (isHovered() || isFocused());
        boolean tradeRow = ReGlassConfig.INSTANCE.features.containers && width >= 80 && width <= 100 && height == 20;
        ReGlassApi.create(context).fromWidget(this).text(null)
                .position(getX(), getY() + (tradeRow ? 1 : 0))
                .size(width, height - (tradeRow ? 2 : 0))
                .hover(hovered ? 1f : 0f)
                .focus(held && isHovered() ? 1f : 0f)
                .style(WidgetStyle.create().tint(active ? 0xFFFFFFFF : 0xFF000000, active ? 0f : 0.4f))
                .render();
        ci.cancel();
    }
}
