package restudio.reglass.mixin.accessor;

import net.minecraft.client.gui.widget.SliderWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SliderWidget.class)
public interface SliderWidgetAccessor {
    @Accessor("value")
    double getValue();

    @Accessor("value")
    void setValuePublic(double value);
}
