package restudio.reglass.mixin.accessor;

import net.minecraft.client.gui.render.GuiRenderer;
//? if >= 26 {
import net.minecraft.client.renderer.GameRenderer;
//? } else {
/*import net.minecraft.client.render.GameRenderer;
*///? }
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
    @Accessor("guiRenderer")
    GuiRenderer getGuiRenderer();
}
