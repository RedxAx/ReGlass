package restudio.reglass.client.ui;

import java.util.function.Consumer;
//#if MC >= 26
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
//#else
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
//#endif
import restudio.reglass.mixin.accessor.SliderWidgetAccessor;

//#if MC >= 26
public class MappedSlider extends AbstractSliderButton {
//#else
public class MappedSlider extends SliderWidget {
//#endif

    private final double min;
    private final double max;
    private final Consumer<Double> onChange;
    private final boolean integer;
//#if MC >= 26
    private final Component originalMessage = this.getMessage();
//#else
    private final Text originalMessage = this.getMessage();
//#endif

//#if MC >= 26
    public static MappedSlider floatSlider(int x, int y, int width, int height, Component msg, double min, double max, double init, Consumer<Double> onChange) {
//#else
    public static MappedSlider floatSlider(int x, int y, int width, int height, Text msg, double min, double max, double init, Consumer<Double> onChange) {
//#endif
        return new MappedSlider(x, y, width, height, msg, min, max, init, onChange, false);
    }

//#if MC >= 26
    public static MappedSlider intSlider(int x, int y, int width, int height, Component msg, int min, int max, int init, Consumer<Integer> onChange) {
//#else
    public static MappedSlider intSlider(int x, int y, int width, int height, Text msg, int min, int max, int init, Consumer<Integer> onChange) {
//#endif
        return new MappedSlider(x, y, width, height, msg, min, max, init, d -> onChange.accept(d.intValue()), true);
    }

//#if MC >= 26
    private MappedSlider(int x, int y, int width, int height, Component message, double min, double max, double init, Consumer<Double> onChange, boolean integer) {
//#else
    private MappedSlider(int x, int y, int width, int height, Text message, double min, double max, double init, Consumer<Double> onChange, boolean integer) {
//#endif
        super(x, y, width, height, message, 0);
        this.min = min;
        this.max = max;
        this.onChange = onChange;
        this.integer = integer;
        ((SliderWidgetAccessor) this).setValuePublic(inverseMap(init));
        updateMessage();
    }

    private double map(double v) {
        return min + v * (max - min);
    }

    private double inverseMap(double real) {
        if (max == min) return 0;
        return (real - min) / (max - min);
    }

    @Override
    protected void updateMessage() {
        double v = map(this.value);
        if (integer) v = Math.round(v);
//#if MC >= 26
        this.setMessage(Component.literal(originalMessage.getString() + ": " + format(v)));
//#else
        this.setMessage(Text.literal(originalMessage.getString() + ": " + format(v)));
//#endif
    }

    private String format(double v) {
        if (integer) return Integer.toString((int) Math.round(v));
        return String.format("%.3f", v);
    }

    @Override
    protected void applyValue() {
        double v = map(this.value);
        if (integer) v = Math.round(v);
        onChange.accept(v);
    }
}
