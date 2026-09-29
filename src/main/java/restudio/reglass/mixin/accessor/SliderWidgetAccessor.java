package restudio.reglass.mixin.accessor;

//#if MC >= 26
import net.minecraft.client.gui.components.AbstractSliderButton;
//#else
import net.minecraft.client.gui.widget.SliderWidget;
//#endif
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//#if MC >= 26
@Mixin(AbstractSliderButton.class)
//#else
@Mixin(SliderWidget.class)
//#endif
public interface SliderWidgetAccessor {
    @Accessor("value")
    double getValue();

    @Accessor("value")
    void setValuePublic(double value);
}

