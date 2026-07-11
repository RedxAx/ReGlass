package restudio.reglass.mixin.accessor;

import net.minecraft.client.gui.widget.TextIconButtonWidget;
//? if >= 26 {
import net.minecraft.resources.Identifier;
//? } else {
/*import net.minecraft.util.Identifier;
*///? }
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TextIconButtonWidget.class)
public interface TextIconButtonWidgetAccessor {
    @Accessor("textureWidth")
    int getTextureWidth();

    @Accessor("textureHeight")
    int getTextureHeight();

}
