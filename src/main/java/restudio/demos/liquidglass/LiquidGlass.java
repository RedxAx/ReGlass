package restudio.demos.liquidglass;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiquidGlass implements ModInitializer {
    public static final String MOD_ID = "liquidglass";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Init LiquidGlass");
    }
}