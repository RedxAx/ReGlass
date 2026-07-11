package restudio.reglass.client.screen.config;

import java.util.ArrayList;
import java.util.List;
//? if >= 26 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
//? } else {
/*import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
*///? }
import restudio.reglass.client.LiquidGlassWidget;
import restudio.reglass.client.api.ReGlassConfig;
import restudio.reglass.client.api.WidgetStyle;
import restudio.reglass.client.config.ReGlassSettingsIO;
import restudio.reglass.client.ui.MappedSlider;

public class ReGlassConfigScreen extends Screen {
    private final Screen parent;
    private final List<PositionedWidget> positionedWidgets = new ArrayList<>();
    private LiquidGlassWidget previewCircle;
    private LiquidGlassWidget previewRounded;
    private double scrollPosition;
    private int totalListHeight;

//? if >= 26 {
    private record PositionedWidget(AbstractWidget widget, int y) {}
//? } else {
    /*private record PositionedWidget(ClickableWidget widget, int y) {}
*///? }

    public ReGlassConfigScreen(Screen parent) {
//? if >= 26 {
        super(Component.literal("ReGlass Configuration"));
//? } else {
        /*super(Text.literal("ReGlass Configuration"));
*///? }
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        positionedWidgets.clear();

        int listWidth = Math.min(300, this.width / 2 - 20);
        int widgetWidth = listWidth - 20;
        int widgetX = 10 + 10;

        int y = 5;
        int gap = 4;
        int widgetHeight = 20;

        ReGlassConfig cfg = ReGlassConfig.INSTANCE;

        addTitle("General", widgetX, y, widgetWidth);
        y += widgetHeight;

//? if >= 26 {
        Button enableRedesignButton = Button.builder(getEnableRedesignText(), button -> {
//? } else {
        /*ButtonWidget enableRedesignButton = ButtonWidget.builder(getEnableRedesignText(), button -> {
*///? }
            cfg.features.enableRedesign = !cfg.features.enableRedesign;
            button.setMessage(getEnableRedesignText());
//? if >= 26.2 {
            this.minecraft.gui.setScreen(new ReGlassConfigScreen(this.parent));
//? } elif >= 26 {
            /*this.minecraft.setScreen(new ReGlassConfigScreen(this.parent));
*///? } else {
            /*this.client.setScreen(new ReGlassConfigScreen(this.parent));
*///? }
//? if >= 26 {
        }).bounds(widgetX, y, widgetWidth, widgetHeight).build();
//? } else {
        /*}).dimensions(widgetX, y, widgetWidth, widgetHeight).build();
*///? }
        addPositionedWidget(enableRedesignButton, y);
        y += widgetHeight + gap;

//? if >= 26 {
        Button enableButtonsButton = Button.builder(getFeatureText("Buttons", cfg.features.buttons), button -> {
//? } else {
        /*ButtonWidget enableButtonsButton = ButtonWidget.builder(getFeatureText("Buttons", cfg.features.buttons), button -> {
*///? }
            cfg.features.buttons = !cfg.features.buttons;
            button.setMessage(getFeatureText("Buttons", cfg.features.buttons));
//? if >= 26 {
        }).bounds(widgetX, y, widgetWidth, widgetHeight).build();
//? } else {
        /*}).dimensions(widgetX, y, widgetWidth, widgetHeight).build();
*///? }
        addPositionedWidget(enableButtonsButton, y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap;

//? if >= 26 {
        Button enableSlidersButton = Button.builder(getFeatureText("Sliders", cfg.features.sliders), button -> {
//? } else {
        /*ButtonWidget enableSlidersButton = ButtonWidget.builder(getFeatureText("Sliders", cfg.features.sliders), button -> {
*///? }
            cfg.features.sliders = !cfg.features.sliders;
            button.setMessage(getFeatureText("Sliders", cfg.features.sliders));
//? if >= 26 {
        }).bounds(widgetX, y, widgetWidth, widgetHeight).build();
//? } else {
        /*}).dimensions(widgetX, y, widgetWidth, widgetHeight).build();
*///? }
        addPositionedWidget(enableSlidersButton, y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap;

//? if >= 26 {
        Button enableHotbarButton = Button.builder(getFeatureText("Hotbar", cfg.features.hotbar), button -> {
//? } else {
        /*ButtonWidget enableHotbarButton = ButtonWidget.builder(getFeatureText("Hotbar", cfg.features.hotbar), button -> {
*///? }
            cfg.features.hotbar = !cfg.features.hotbar;
            button.setMessage(getFeatureText("Hotbar", cfg.features.hotbar));
//? if >= 26 {
        }).bounds(widgetX, y, widgetWidth, widgetHeight).build();
//? } else {
        /*}).dimensions(widgetX, y, widgetWidth, widgetHeight).build();
*///? }
        addPositionedWidget(enableHotbarButton, y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap;

//? if >= 26 {
        Button enableContainersButton = Button.builder(getFeatureText("Containers", cfg.features.containers), button -> {
            cfg.features.containers = !cfg.features.containers;
            button.setMessage(getFeatureText("Containers", cfg.features.containers));
        }).bounds(widgetX, y, widgetWidth, widgetHeight).build();
        addPositionedWidget(enableContainersButton, y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap;

        Button containerTextPanelsButton = Button.builder(getFeatureText("Container Text Panels", cfg.features.containerTextPanels), button -> {
            cfg.features.containerTextPanels = !cfg.features.containerTextPanels;
            button.setMessage(getFeatureText("Container Text Panels", cfg.features.containerTextPanels));
        }).bounds(widgetX, y, widgetWidth, widgetHeight).build();
        addPositionedWidget(containerTextPanelsButton, y).active = cfg.features.enableRedesign && cfg.features.containers;
        y += widgetHeight + gap;

        Button cancelDarkeningButton = Button.builder(getFeatureText("Cancel Screen Darkening", cfg.features.cancelScreenDarkening), button -> {
//? } else {
        /*ButtonWidget cancelDarkeningButton = ButtonWidget.builder(getFeatureText("Cancel Screen Darkening", cfg.features.cancelScreenDarkening), button -> {
*///? }
            cfg.features.cancelScreenDarkening = !cfg.features.cancelScreenDarkening;
            button.setMessage(getFeatureText("Cancel Screen Darkening", cfg.features.cancelScreenDarkening));
//? if >= 26 {
        }).bounds(widgetX, y, widgetWidth, widgetHeight).build();
//? } else {
        /*}).dimensions(widgetX, y, widgetWidth, widgetHeight).build();
*///? }
        addPositionedWidget(cancelDarkeningButton, y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap * 2;

        addTitle("Appearance", widgetX, y, widgetWidth);
        y += widgetHeight;

//? if >= 26 {
        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.literal("Tint Alpha"), 0f, 1f, cfg.defaultTintAlpha, v -> cfg.defaultTintAlpha = v.floatValue()), y).active = cfg.features.enableRedesign;
//? } else {
        /*addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Text.literal("Tint Alpha"), 0f, 1f, cfg.defaultTintAlpha, v -> cfg.defaultTintAlpha = v.floatValue()), y).active = cfg.features.enableRedesign;
*///? }
        y += widgetHeight + gap;
//? if >= 26 {
        addSlider(MappedSlider.intSlider(widgetX, y, widgetWidth, widgetHeight, Component.literal("Blur Radius"), 0, 32, cfg.defaultBlurRadius, v -> cfg.defaultBlurRadius = v), y).active = cfg.features.enableRedesign;
//? } else {
        /*addSlider(MappedSlider.intSlider(widgetX, y, widgetWidth, widgetHeight, Text.literal("Blur Radius"), 0, 32, cfg.defaultBlurRadius, v -> cfg.defaultBlurRadius = v), y).active = cfg.features.enableRedesign;
*///? }
        y += widgetHeight + gap;
//? if >= 26 {
        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.literal("Smoothing"), -0.02f, 0.02f, cfg.defaultSmoothing, v -> cfg.defaultSmoothing = v.floatValue()), y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap;
        addSlider(MappedSlider.intSlider(widgetX, y, widgetWidth, widgetHeight, Component.literal("Default Layer"), 0, 15, cfg.defaultLayer, v -> cfg.defaultLayer = v), y).active = cfg.features.enableRedesign;
//? } else {
        /*addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Text.literal("Smoothing"), -0.02f, 0.02f, cfg.defaultSmoothing, v -> cfg.defaultSmoothing = v.floatValue()), y).active = cfg.features.enableRedesign;
*///? }
        y += widgetHeight + gap * 2;

        addTitle("Shadow", widgetX, y, widgetWidth);
        y += widgetHeight;

//? if >= 26 {
        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.literal("Shadow Expand"), 0f, 100f, cfg.defaultShadowExpand, v -> cfg.defaultShadowExpand = v.floatValue()), y).active = cfg.features.enableRedesign;
//? } else {
        /*addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Text.literal("Shadow Expand"), 0f, 100f, cfg.defaultShadowExpand, v -> cfg.defaultShadowExpand = v.floatValue()), y).active = cfg.features.enableRedesign;
*///? }
        y += widgetHeight + gap;
//? if >= 26 {
        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.literal("Shadow Factor"), 0f, 1f, cfg.defaultShadowFactor, v -> cfg.defaultShadowFactor = v.floatValue()), y).active = cfg.features.enableRedesign;
//? } else {
        /*addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Text.literal("Shadow Factor"), 0f, 1f, cfg.defaultShadowFactor, v -> cfg.defaultShadowFactor = v.floatValue()), y).active = cfg.features.enableRedesign;
*///? }
        y += widgetHeight + gap;
//? if >= 26 {
        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.literal("Shadow Offset Y"), -10f, 10f, cfg.defaultShadowOffsetY, v -> cfg.defaultShadowOffsetY = v.floatValue()), y).active = cfg.features.enableRedesign;
//? } else {
        /*addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Text.literal("Shadow Offset Y"), -10f, 10f, cfg.defaultShadowOffsetY, v -> cfg.defaultShadowOffsetY = v.floatValue()), y).active = cfg.features.enableRedesign;
*///? }
        y += widgetHeight + gap * 2;

        addTitle("Refraction", widgetX, y, widgetWidth);
        y += widgetHeight;

//? if >= 26 {
        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.literal("Refraction Thickness"), 1f, 60f, cfg.defaultRefThickness, v -> cfg.defaultRefThickness = v.floatValue()), y).active = cfg.features.enableRedesign;
//? } else {
        /*addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Text.literal("Refraction Thickness"), 1f, 60f, cfg.defaultRefThickness, v -> cfg.defaultRefThickness = v.floatValue()), y).active = cfg.features.enableRedesign;
*///? }
        y += widgetHeight + gap;
//? if >= 26 {
        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.literal("Refraction Factor"), 1.0f, 2.5f, cfg.defaultRefFactor, v -> cfg.defaultRefFactor = v.floatValue()), y).active = cfg.features.enableRedesign;
//? } else {
        /*addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Text.literal("Refraction Factor"), 1.0f, 2.5f, cfg.defaultRefFactor, v -> cfg.defaultRefFactor = v.floatValue()), y).active = cfg.features.enableRedesign;
*///? }
        y += widgetHeight + gap;
//? if >= 26 {
        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.literal("Fresnel Range"), 0f, 60f, cfg.defaultRefFresnelRange, v -> cfg.defaultRefFresnelRange = v.floatValue()), y).active = cfg.features.enableRedesign;
//? } else {
        /*addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Text.literal("Fresnel Range"), 0f, 60f, cfg.defaultRefFresnelRange, v -> cfg.defaultRefFresnelRange = v.floatValue()), y).active = cfg.features.enableRedesign;
*///? }
        y += widgetHeight + gap;
//? if >= 26 {
        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.literal("Fresnel Hardness"), 0f, 100f, cfg.defaultRefFresnelHardness, v -> cfg.defaultRefFresnelHardness = v.floatValue()), y).active = cfg.features.enableRedesign;
//? } else {
        /*addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Text.literal("Fresnel Hardness"), 0f, 100f, cfg.defaultRefFresnelHardness, v -> cfg.defaultRefFresnelHardness = v.floatValue()), y).active = cfg.features.enableRedesign;
*///? }
        y += widgetHeight + gap;
//? if >= 26 {
        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.literal("Fresnel Factor"), 0f, 100f, cfg.defaultRefFresnelFactor, v -> cfg.defaultRefFresnelFactor = v.floatValue()), y).active = cfg.features.enableRedesign;
//? } else {
        /*addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Text.literal("Fresnel Factor"), 0f, 100f, cfg.defaultRefFresnelFactor, v -> cfg.defaultRefFresnelFactor = v.floatValue()), y).active = cfg.features.enableRedesign;
*///? }
        y += widgetHeight + gap * 2;

        addTitle("Glare", widgetX, y, widgetWidth);
        y += widgetHeight;

//? if >= 26 {
        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.literal("Glare Range"), 0f, 60f, cfg.defaultGlareRange, v -> cfg.defaultGlareRange = v.floatValue()), y).active = cfg.features.enableRedesign;
//? } else {
        /*addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Text.literal("Glare Range"), 0f, 60f, cfg.defaultGlareRange, v -> cfg.defaultGlareRange = v.floatValue()), y).active = cfg.features.enableRedesign;
*///? }
        y += widgetHeight + gap;
//? if >= 26 {
        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.literal("Glare Factor"), 0f, 100f, cfg.defaultGlareFactor, v -> cfg.defaultGlareFactor = v.floatValue()), y).active = cfg.features.enableRedesign;
//? } else {
        /*addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Text.literal("Glare Factor"), 0f, 100f, cfg.defaultGlareFactor, v -> cfg.defaultGlareFactor = v.floatValue()), y).active = cfg.features.enableRedesign;
*///? }
        y += widgetHeight + gap * 2;

        addTitle("Interactions", widgetX, y, widgetWidth);
        y += widgetHeight;

//? if >= 26 {
        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.literal("Hover Scale (px)"), 0f, 6f, cfg.hoverScalePx, v -> cfg.hoverScalePx = v.floatValue()), y).active = cfg.features.enableRedesign;
//? } else {
        /*addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Text.literal("Hover Scale (px)"), 0f, 6f, cfg.hoverScalePx, v -> cfg.hoverScalePx = v.floatValue()), y).active = cfg.features.enableRedesign;
*///? }
        y += widgetHeight + gap;
//? if >= 26 {
        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.literal("Focus Scale (px)"), 0f, 8f, cfg.focusScalePx, v -> cfg.focusScalePx = v.floatValue()), y).active = cfg.features.enableRedesign;
//? } else {
        /*addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Text.literal("Focus Scale (px)"), 0f, 8f, cfg.focusScalePx, v -> cfg.focusScalePx = v.floatValue()), y).active = cfg.features.enableRedesign;
*///? }
        y += widgetHeight + gap;
//? if >= 26 {
        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.literal("Focus Border Width (px)"), 0f, 6f, cfg.focusBorderWidthPx, v -> cfg.focusBorderWidthPx = v.floatValue()), y).active = cfg.features.enableRedesign;
//? } else {
        /*addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Text.literal("Focus Border Width (px)"), 0f, 6f, cfg.focusBorderWidthPx, v -> cfg.focusBorderWidthPx = v.floatValue()), y).active = cfg.features.enableRedesign;
*///? }
        y += widgetHeight + gap;
//? if >= 26 {
        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.literal("Focus Border Intensity"), 0f, 1f, cfg.focusBorderIntensity, v -> cfg.focusBorderIntensity = v.floatValue()), y).active = cfg.features.enableRedesign;
//? } else {
        /*addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Text.literal("Focus Border Intensity"), 0f, 1f, cfg.focusBorderIntensity, v -> cfg.focusBorderIntensity = v.floatValue()), y).active = cfg.features.enableRedesign;
*///? }
        y += widgetHeight + gap;
//? if >= 26 {
        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.literal("Focus Border Speed"), 0f, 4f, cfg.focusBorderSpeed, v -> cfg.focusBorderSpeed = v.floatValue()), y).active = cfg.features.enableRedesign;
//? } else {
        /*addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Text.literal("Focus Border Speed"), 0f, 4f, cfg.focusBorderSpeed, v -> cfg.focusBorderSpeed = v.floatValue()), y).active = cfg.features.enableRedesign;
*///? }
        y += widgetHeight + gap * 2;

        addTitle("Debug", widgetX, y, widgetWidth);
        y += widgetHeight;

//? if >= 26 {
        Button pixelatedGridButton = Button.builder(getFeatureText("Pixelated Grid", cfg.features.pixelatedGrid), button -> {
//? } else {
        /*ButtonWidget pixelatedGridButton = ButtonWidget.builder(getFeatureText("Pixelated Grid", cfg.features.pixelatedGrid), button -> {
*///? }
            cfg.features.pixelatedGrid = !cfg.features.pixelatedGrid;
            button.setMessage(getFeatureText("Pixelated Grid", cfg.features.pixelatedGrid));
//? if >= 26 {
        }).bounds(widgetX, y, widgetWidth, widgetHeight).build();
//? } else {
        /*}).dimensions(widgetX, y, widgetWidth, widgetHeight).build();
*///? }
        addPositionedWidget(pixelatedGridButton, y).active = cfg.features.enableRedesign;
        y += widgetHeight + gap;

//? if >= 26 {
        addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Component.literal("Grid Size"), 1f, 32f, cfg.pixelatedGridSize, v -> cfg.pixelatedGridSize = v.floatValue()), y).active = cfg.features.enableRedesign;
//? } else {
        /*addSlider(MappedSlider.floatSlider(widgetX, y, widgetWidth, widgetHeight, Text.literal("Grid Size"), 1f, 32f, cfg.pixelatedGridSize, v -> cfg.pixelatedGridSize = v.floatValue()), y).active = cfg.features.enableRedesign;
*///? }
        y += widgetHeight + gap;
//? if >= 26 {
        addSlider(MappedSlider.intSlider(widgetX, y, widgetWidth, widgetHeight, Component.literal("Debug Step"), 0, 9, Math.round(cfg.debugStep), v -> cfg.debugStep = v.floatValue()), y).active = cfg.features.enableRedesign;
//? } else {
        /*addSlider(MappedSlider.intSlider(widgetX, y, widgetWidth, widgetHeight, Text.literal("Debug Step"), 0, 9, Math.round(cfg.debugStep), v -> cfg.debugStep = v.floatValue()), y).active = cfg.features.enableRedesign;
*///? }
        y += widgetHeight + gap;

        this.totalListHeight = y;

//? if >= 26 {
        addRenderableWidget(Button.builder(Component.translatable("controls.reset"), b -> {
//? } else {
        /*addDrawableChild(ButtonWidget.builder(Text.translatable("controls.reset"), b -> {
*///? }
            ReGlassSettingsIO.apply(new ReGlassSettingsIO.Data());
//? if >= 26.2 {
            if (this.minecraft != null) {
                this.minecraft.gui.setScreen(new ReGlassConfigScreen(this.parent));
//? } elif >= 26 {
            /*if (this.minecraft != null) {
                this.minecraft.setScreen(new ReGlassConfigScreen(this.parent));
*///? } else {
            /*if (this.client != null) {
                this.client.setScreen(new ReGlassConfigScreen(this.parent));
*///? }
            }
//? if >= 26 {
        }).bounds(this.width / 2 - 100, this.height - 28, 98, 20).build());
//? } else {
        /*}).dimensions(this.width / 2 - 100, this.height - 28, 98, 20).build());
*///? }

//? if >= 26 {
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose()).bounds(this.width / 2 + 2, this.height - 28, 98, 20).build());
//? } else {
        /*addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), b -> close()).dimensions(this.width / 2 + 2, this.height - 28, 98, 20).build());
*///? }

        int previewX = this.width / 2 + 20;
        int previewY = this.height / 2 - 50;

        WidgetStyle s1 = WidgetStyle.create().tint(0xFFFFFF, Math.min(1f, Math.max(0f, cfg.defaultTintAlpha))).blurRadius(cfg.defaultBlurRadius)
                .shadow(cfg.defaultShadowExpand, cfg.defaultShadowFactor, cfg.defaultShadowOffsetX, cfg.defaultShadowOffsetY)
                .shadowColor(cfg.defaultShadowColor, cfg.defaultShadowColorAlpha)
                .refractionThickness(cfg.defaultRefThickness).refractionFactor(cfg.defaultRefFactor).refractionDispersion(cfg.defaultRefDispersion)
                .fresnelRange(cfg.defaultRefFresnelRange).fresnelHardness(cfg.defaultRefFresnelHardness).fresnelFactor(cfg.defaultRefFresnelFactor)
                .glareRange(cfg.defaultGlareRange).glareHardness(cfg.defaultGlareHardness).glareConvergence(cfg.defaultGlareConvergence)
//? if >= 26 {
                .glareOppositeFactor(cfg.defaultGlareOppositeFactor).glareFactor(cfg.defaultGlareFactor).glareAngleRad(cfg.defaultGlareAngleRad)
                .layer(1);
//? } else {
                /*.glareOppositeFactor(cfg.defaultGlareOppositeFactor).glareFactor(cfg.defaultGlareFactor).glareAngleRad(cfg.defaultGlareAngleRad);
*///? }

//? if >= 26 {
        previewCircle = addRenderableWidget(new LiquidGlassWidget(previewX + 40, previewY, 100, 100, s1).setCornerRadiusPx(50f));
//? } else {
        /*previewCircle = addDrawableChild(new LiquidGlassWidget(previewX, previewY, 100, 100, s1).setCornerRadiusPx(50f));
*///? }

        WidgetStyle s2 = WidgetStyle.create().tint(cfg.defaultTintColor, cfg.defaultTintAlpha).blurRadius(cfg.defaultBlurRadius)
                .shadow(cfg.defaultShadowExpand, cfg.defaultShadowFactor, cfg.defaultShadowOffsetX, cfg.defaultShadowOffsetY)
//? if >= 26 {
                .shadowColor(cfg.defaultShadowColor, cfg.defaultShadowColorAlpha)
                .layer(0);
//? } else {
                /*.shadowColor(cfg.defaultShadowColor, cfg.defaultShadowColorAlpha);
*///? }

//? if >= 26 {
        previewRounded = addRenderableWidget(new LiquidGlassWidget(previewX + 20, previewY + 120, 140, 60, s2).setCornerRadiusPx(16f));
//? } else {
        /*previewRounded = addDrawableChild(new LiquidGlassWidget(previewX + 110, previewY + 20, 140, 60, s2).setCornerRadiusPx(16f));
*///? }
    }

//? if >= 26 {
    private Component getEnableRedesignText() {
        return Component.literal("ReGlass Redesign: ").append(ReGlassConfig.INSTANCE.features.enableRedesign ? Component.translatable("options.on") : Component.translatable("options.off"));
//? } else {
    /*private Text getEnableRedesignText() {
        return Text.literal("ReGlass Redesign: ").append(ReGlassConfig.INSTANCE.features.enableRedesign ? Text.translatable("options.on") : Text.translatable("options.off"));
*///? }
    }

//? if >= 26 {
    private Component getFeatureText(String feature, boolean enabled) {
        return Component.literal(feature + ": ").append(enabled ? Component.translatable("options.on") : Component.translatable("options.off"));
//? } else {
    /*private Text getFeatureText(String feature, boolean enabled) {
        return Text.literal(feature + ": ").append(enabled ? Text.translatable("options.on") : Text.translatable("options.off"));
*///? }
    }

//? if >= 26 {
    private <T extends AbstractWidget> T addPositionedWidget(T widget, int y) {
//? } else {
    /*private <T extends ClickableWidget> T addPositionedWidget(T widget, int y) {
*///? }
        positionedWidgets.add(new PositionedWidget(widget, y));
//? if >= 26 {
        return addRenderableWidget(widget);
//? } else {
        /*return addDrawableChild(widget);
*///? }
    }

    private MappedSlider addSlider(MappedSlider slider, int y) {
        return addPositionedWidget(slider, y);
    }

    private void addTitle(String title, int x, int y, int width) {
//? if >= 26 {
        addPositionedWidget(new TitleWidget(x, y, width, 20, Component.literal(title)), y);
//? } else {
        /*addPositionedWidget(new TitleWidget(x, y, width, 20, Text.literal(title)), y);
*///? }
    }

    @Override
//? if >= 26 {
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
//? } else {
    /*public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

*///? }
        int listTop = 32;
        int listBottom = this.height - 32;

        for (PositionedWidget pw : positionedWidgets) {
            pw.widget.setY(pw.y() + listTop - (int) this.scrollPosition);
            if (pw.widget() instanceof TitleWidget tw) {
                tw.visible = (pw.widget.getY() >= listTop && (pw.widget.getY() + 20) <= listBottom);
            } else {
                pw.widget.visible = (pw.widget.getY() >= listTop && (pw.widget.getY() + pw.widget.getHeight()) <= listBottom);
            }
        }

//? if >= 26 {
        super.extractRenderState(context, mouseX, mouseY, delta);
//? } else {
        /*super.render(context, mouseX, mouseY, delta);
*///? }

//? if >= 26 {
        context.centeredText(this.font, Component.literal("ReGlass Config (Scrollable)"), this.width / 2, 15, 0xFFFFFFFF);
//? } else {
        /*context.drawCenteredTextWithShadow(this.textRenderer, Text.of("ReGlass Config (Scrollable)"), this.width / 2, 15, 0xFFFFFFFF);
*///? }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int listHeight = (this.height - 32) - 32;
        int maxScroll = Math.max(0, this.totalListHeight - listHeight);
        if (maxScroll > 0) {
            this.scrollPosition -= verticalAmount * 10;
//? if >= 26 {
            this.scrollPosition = Mth.clamp(this.scrollPosition, 0, maxScroll);
//? } else {
            /*this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0, maxScroll);
*///? }
            return true;
        }
        return false;
    }

    @Override
//? if >= 26 {
    public void onClose() {
//? } else {
    /*public void close() {
*///? }
        ReGlassSettingsIO.saveFromMemory();
//? if >= 26.2 {
        if (this.minecraft != null) {
            this.minecraft.gui.setScreen(this.parent);
//? } elif >= 26 {
        /*if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
*///? } else {
        /*if (this.client != null) {
            this.client.setScreen(this.parent);
*///? }
        }
    }

//? if >= 26 {
    private class TitleWidget extends AbstractWidget {
        public TitleWidget(int x, int y, int width, int height, Component message) {
//? } else {
    /*private class TitleWidget extends ClickableWidget {
        public TitleWidget(int x, int y, int width, int height, Text message) {
*///? }
            super(x, y, width, height, message);
        }

        @Override
//? if >= 26 {
        public void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
//? } else {
        /*public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
*///? }
            if (this.visible) {
//? if >= 26 {
                context.centeredText(ReGlassConfigScreen.this.font, this.getMessage(), this.getX() + this.getWidth() / 2, this.getY() + (this.getHeight() - 8) / 2, 0xFFFFFF);
//? } else {
                /*context.drawCenteredTextWithShadow(ReGlassConfigScreen.this.textRenderer, this.getMessage(), this.getX() + this.getWidth() / 2, this.getY() + (this.getHeight() - 8) / 2, 0xFFFFFF);
*///? }
            }
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return false;
        }

        @Override
//? if >= 26 {
        protected void updateWidgetNarration(NarrationElementOutput builder) {}
//? } else {
        /*protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
*///? }
    }
}
