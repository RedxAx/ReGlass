//? if >=26 {
package restudio.reglass.mixin.accessor;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "net.minecraft.client.gui.GuiGraphicsExtractor$ScissorStack")
public interface ScissorStackAccessor {
    @Invoker("peek")
    ScreenRectangle reglass$peek();
}
//?}
