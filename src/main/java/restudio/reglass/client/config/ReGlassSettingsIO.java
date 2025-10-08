package restudio.reglass.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import restudio.reglass.client.api.ReGlassConfig;

public final class ReGlassSettingsIO {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("reglass.json");
    }

    public static void loadIntoMemory() {
        try {
            Path p = configPath();
            if (!Files.exists(p)) {
                saveFromMemory();
                return;
            }
            try (Reader r = Files.newBufferedReader(p)) {
                Data d = GSON.fromJson(r, Data.class);
                if (d == null) return;
                apply(d);
            }
        } catch (Exception ignored) {
        }
    }

    public static void saveFromMemory() {
        try {
            Path p = configPath();
            Data d = snapshot();
            try (Writer w = Files.newBufferedWriter(p)) {
                GSON.toJson(d, w);
            }
        } catch (Exception ignored) {
        }
    }

    private static Data snapshot() {
        ReGlassConfig c = ReGlassConfig.INSTANCE;
        Data d = new Data();
        d.features_enableRedesign = c.features.enableRedesign;
        d.features_buttons = c.features.buttons;
        d.features_sliders = c.features.sliders;
        d.features_hotbar = c.features.hotbar;
        d.features_cancelScreenDarkening = c.features.cancelScreenDarkening;

        d.tintColor = c.defaultTintColor;
        d.tintAlpha = c.defaultTintAlpha;
        d.smoothing = c.defaultSmoothing;
        d.blurRadius = c.defaultBlurRadius;

        d.shadowExpand = c.defaultShadowExpand;
        d.shadowFactor = c.defaultShadowFactor;
        d.shadowOffsetX = c.defaultShadowOffsetX;
        d.shadowOffsetY = c.defaultShadowOffsetY;
        d.shadowColor = c.defaultShadowColor;
        d.shadowColorAlpha = c.defaultShadowColorAlpha;

        d.refThickness = c.defaultRefThickness;
        d.refFactor = c.defaultRefFactor;
        d.refDispersion = c.defaultRefDispersion;
        d.refFresnelRange = c.defaultRefFresnelRange;
        d.refFresnelHardness = c.defaultRefFresnelHardness;
        d.refFresnelFactor = c.defaultRefFresnelFactor;

        d.glareRange = c.defaultGlareRange;
        d.glareHardness = c.defaultGlareHardness;
        d.glareConvergence = c.defaultGlareConvergence;
        d.glareOppositeFactor = c.defaultGlareOppositeFactor;
        d.glareFactor = c.defaultGlareFactor;
        d.glareAngleRad = c.defaultGlareAngleRad;

        return d;
    }

    private static void apply(Data d) {
        ReGlassConfig c = ReGlassConfig.INSTANCE;

        c.features.enableRedesign = d.features_enableRedesign;
        c.features.buttons = d.features_buttons;
        c.features.sliders = d.features_sliders;
        c.features.hotbar = d.features_hotbar;
        c.features.cancelScreenDarkening = d.features_cancelScreenDarkening;

        c.defaultTintColor = d.tintColor;
        c.defaultTintAlpha = d.tintAlpha;
        c.defaultSmoothing = d.smoothing;
        c.defaultBlurRadius = d.blurRadius;

        c.defaultShadowExpand = d.shadowExpand;
        c.defaultShadowFactor = d.shadowFactor;
        c.defaultShadowOffsetX = d.shadowOffsetX;
        c.defaultShadowOffsetY = d.shadowOffsetY;
        c.defaultShadowColor = d.shadowColor;
        c.defaultShadowColorAlpha = d.shadowColorAlpha;

        c.defaultRefThickness = d.refThickness;
        c.defaultRefFactor = d.refFactor;
        c.defaultRefDispersion = d.refDispersion;
        c.defaultRefFresnelRange = d.refFresnelRange;
        c.defaultRefFresnelHardness = d.refFresnelHardness;
        c.defaultRefFresnelFactor = d.refFresnelFactor;

        c.defaultGlareRange = d.glareRange;
        c.defaultGlareHardness = d.glareHardness;
        c.defaultGlareConvergence = d.glareConvergence;
        c.defaultGlareOppositeFactor = d.glareOppositeFactor;
        c.defaultGlareFactor = d.glareFactor;
        c.defaultGlareAngleRad = d.glareAngleRad;
    }

    public static class Data {
        public boolean features_enableRedesign = true;
        public boolean features_buttons = true;
        public boolean features_sliders = true;
        public boolean features_hotbar = true;
        public boolean features_cancelScreenDarkening = true;

        public int tintColor = 0x000000;
        public float tintAlpha = 0f;
        public float smoothing = 0.003f;
        public int blurRadius = 3;

        public float shadowExpand = 25.0f;
        public float shadowFactor = 0.15f;
        public float shadowOffsetX = 0.0f;
        public float shadowOffsetY = 2.0f;
        public int shadowColor = 0x000000;
        public float shadowColorAlpha = 1.0f;

        public float refThickness = 20.0f;
        public float refFactor = 1.4f;
        public float refDispersion = 7.0f;
        public float refFresnelRange = 30.0f;
        public float refFresnelHardness = 20.0f;
        public float refFresnelFactor = 20.0f;

        public float glareRange = 30.0f;
        public float glareHardness = 20.0f;
        public float glareConvergence = 50.0f;
        public float glareOppositeFactor = 80.0f;
        public float glareFactor = 90.0f;
        public float glareAngleRad = (float) (-45.0 * Math.PI / 180.0);
    }
}