package restudio.reglass.client.screen.config;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import restudio.reglass.client.LiquidGlassWidget;
import restudio.reglass.client.api.ReGlassConfig;
import restudio.reglass.client.api.WidgetStyle;
import restudio.reglass.client.config.ReGlassSettingsIO;
import restudio.reglass.client.ui.MappedSlider;

public class ReGlassConfigScreen extends Screen {
    private final Screen parent;
    private final List<MappedSlider> sliders = new ArrayList<>();
    private LiquidGlassWidget previewCircle;
    private LiquidGlassWidget previewRounded;

    public ReGlassConfigScreen(Screen parent) {
        super(Text.literal("ReGlass Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int left = 16, top = 40, w = 180, h = 20, gap = 22;
        ReGlassConfig cfg = ReGlassConfig.INSTANCE;
        sliders.clear();

        sliders.add(addDrawableChild(MappedSlider.floatSlider(left, top, w, h, Text.literal("Tint Alpha"), 0f, 1f, cfg.defaultTintAlpha, v -> cfg.defaultTintAlpha = v.floatValue()))); top += gap;
        sliders.add(addDrawableChild(MappedSlider.intSlider(left, top, w, h, Text.literal("Blur Radius"), 0, 32, cfg.defaultBlurRadius, v -> cfg.defaultBlurRadius = v))); top += gap;
        sliders.add(addDrawableChild(MappedSlider.floatSlider(left, top, w, h, Text.literal("Smoothing"), -0.02f, 0.02f, cfg.defaultSmoothing, v -> cfg.defaultSmoothing = v.floatValue()))); top += gap;
        sliders.add(addDrawableChild(MappedSlider.floatSlider(left, top, w, h, Text.literal("Shadow Expand"), 0f, 100f, cfg.defaultShadowExpand, v -> cfg.defaultShadowExpand = v.floatValue()))); top += gap;
        sliders.add(addDrawableChild(MappedSlider.floatSlider(left, top, w, h, Text.literal("Shadow Factor"), 0f, 1f, cfg.defaultShadowFactor, v -> cfg.defaultShadowFactor = v.floatValue()))); top += gap;
        sliders.add(addDrawableChild(MappedSlider.floatSlider(left, top, w, h, Text.literal("Shadow Offset Y"), -10f, 10f, cfg.defaultShadowOffsetY, v -> cfg.defaultShadowOffsetY = v.floatValue()))); top += gap;

        sliders.add(addDrawableChild(MappedSlider.floatSlider(left, top, w, h, Text.literal("Refraction Thickness"), 1f, 60f, cfg.defaultRefThickness, v -> cfg.defaultRefThickness = v.floatValue()))); top += gap;
        sliders.add(addDrawableChild(MappedSlider.floatSlider(left, top, w, h, Text.literal("Refraction Factor"), 1.0f, 2.5f, cfg.defaultRefFactor, v -> cfg.defaultRefFactor = v.floatValue()))); top += gap;
        sliders.add(addDrawableChild(MappedSlider.floatSlider(left, top, w, h, Text.literal("Fresnel Range"), 0f, 60f, cfg.defaultRefFresnelRange, v -> cfg.defaultRefFresnelRange = v.floatValue()))); top += gap;
        sliders.add(addDrawableChild(MappedSlider.floatSlider(left, top, w, h, Text.literal("Fresnel Hardness"), 0f, 100f, cfg.defaultRefFresnelHardness, v -> cfg.defaultRefFresnelHardness = v.floatValue()))); top += gap;
        sliders.add(addDrawableChild(MappedSlider.floatSlider(left, top, w, h, Text.literal("Fresnel Factor"), 0f, 100f, cfg.defaultRefFresnelFactor, v -> cfg.defaultRefFresnelFactor = v.floatValue()))); top += gap;

        sliders.add(addDrawableChild(MappedSlider.floatSlider(left, top, w, h, Text.literal("Glare Range"), 0f, 60f, cfg.defaultGlareRange, v -> cfg.defaultGlareRange = v.floatValue()))); top += gap;
        sliders.add(addDrawableChild(MappedSlider.floatSlider(left, top, w, h, Text.literal("Glare Factor"), 0f, 100f, cfg.defaultGlareFactor, v -> cfg.defaultGlareFactor = v.floatValue()))); top += gap;

        sliders.add(addDrawableChild(MappedSlider.intSlider(left, top, w, h, Text.literal("Debug Step"), 0, 9, Math.round(cfg.debugStep), v -> cfg.debugStep = v.floatValue()))); top += gap;

        addDrawableChild(ButtonWidget.builder(
            Text.literal("Pixelated Grid: " + (cfg.features.pixelatedGrid ? "ON" : "OFF")),
            button -> {
                cfg.features.pixelatedGrid = !cfg.features.pixelatedGrid;
                button.setMessage(Text.literal("Pixelated Grid: " + (cfg.features.pixelatedGrid ? "ON" : "OFF")));
            }
        ).dimensions(left, top, w, h).build());
        top += gap;

        sliders.add(addDrawableChild(MappedSlider.floatSlider(left, top, w, h, Text.literal("Grid Size"), 1f, 32f, cfg.pixelatedGridSize, v -> cfg.pixelatedGridSize = v.floatValue())));
        top += gap;

        int rightCol = this.width - left - w;
        addDrawableChild(ButtonWidget.builder(Text.literal("Save"), b -> ReGlassSettingsIO.saveFromMemory()).dimensions(rightCol, 16, 80, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), b -> { ReGlassSettingsIO.saveFromMemory(); MinecraftClient.getInstance().setScreen(parent); }).dimensions(rightCol + 90, 16, 80, 20).build());

        WidgetStyle s1 = WidgetStyle.create().tint(0xFFFFFF, Math.min(1f, Math.max(0f, cfg.defaultTintAlpha))).blurRadius(cfg.defaultBlurRadius)
                .shadow(cfg.defaultShadowExpand, cfg.defaultShadowFactor, cfg.defaultShadowOffsetX, cfg.defaultShadowOffsetY)
                .shadowColor(cfg.defaultShadowColor, cfg.defaultShadowColorAlpha)
                .refractionThickness(cfg.defaultRefThickness).refractionFactor(cfg.defaultRefFactor).refractionDispersion(cfg.defaultRefDispersion)
                .fresnelRange(cfg.defaultRefFresnelRange).fresnelHardness(cfg.defaultRefFresnelHardness).fresnelFactor(cfg.defaultRefFresnelFactor)
                .glareRange(cfg.defaultGlareRange).glareHardness(cfg.defaultGlareHardness).glareConvergence(cfg.defaultGlareConvergence)
                .glareOppositeFactor(cfg.defaultGlareOppositeFactor).glareFactor(cfg.defaultGlareFactor).glareAngleRad(cfg.defaultGlareAngleRad);

        previewCircle = addDrawableChild(new LiquidGlassWidget(this.width / 2 - 120, this.height / 2 - 50, 100, 100, s1).setCornerRadiusPx(50f));

        WidgetStyle s2 = WidgetStyle.create().tint(cfg.defaultTintColor, cfg.defaultTintAlpha).blurRadius(cfg.defaultBlurRadius)
                .shadow(cfg.defaultShadowExpand, cfg.defaultShadowFactor, cfg.defaultShadowOffsetX, cfg.defaultShadowOffsetY)
                .shadowColor(cfg.defaultShadowColor, cfg.defaultShadowColorAlpha);

        previewRounded = addDrawableChild(new LiquidGlassWidget(this.width / 2 + 20, this.height / 2 - 30, 140, 60, s2).setCornerRadiusPx(16f));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawText(this.textRenderer, Text.literal("ReGlass Config"), 16, 16, 0xFFFFFFFF, true);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        ReGlassSettingsIO.saveFromMemory();
        this.client.setScreen(this.parent);
    }
}