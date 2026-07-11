package restudio.reglass.mixin.accessor;

//? if >= 26 {
import net.minecraft.client.gui.components.AbstractSliderButton;
//? } else {
/*import net.minecraft.client.gui.widget.SliderWidget;
*///? }
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//? if >= 26 {
@Mixin(AbstractSliderButton.class)
//? } else {
/*@Mixin(SliderWidget.class)
*///? }
public interface SliderWidgetAccessor {
    @Accessor("value")
    double getValue();

    @Accessor("value")
    void setValuePublic(double value);
}

