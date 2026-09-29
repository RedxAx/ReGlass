package restudio.reglass.client.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import restudio.reglass.client.screen.config.ReGlassConfigScreen;

public class ReGlassModMenuApi implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ReGlassConfigScreen::new;
    }
}
