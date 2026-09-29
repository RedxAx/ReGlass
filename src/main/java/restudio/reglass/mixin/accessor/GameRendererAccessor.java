package restudio.reglass.mixin.accessor;

import net.minecraft.client.gui.render.GuiRenderer;
//#if MC >= 26
import net.minecraft.client.renderer.GameRenderer;
//#else
import net.minecraft.client.render.GameRenderer;
//#endif
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
    @Accessor("guiRenderer")
    GuiRenderer getGuiRenderer();
}
