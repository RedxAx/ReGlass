package restudio.reglass.client.ui;

import java.util.function.Consumer;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import restudio.reglass.mixin.accessor.SliderWidgetAccessor;

public class MappedSlider extends SliderWidget {

    private final double min;
    private final double max;
    private final Consumer<Double> onChange;
    private final boolean integer;
    private final Text originalMessage = this.getMessage();

    public static MappedSlider floatSlider(int x, int y, int width, int height, Text msg, double min, double max, double init, Consumer<Double> onChange) {
        return new MappedSlider(x, y, width, height, msg, min, max, init, onChange, false);
    }

    public static MappedSlider intSlider(int x, int y, int width, int height, Text msg, int min, int max, int init, Consumer<Integer> onChange) {
        return new MappedSlider(x, y, width, height, msg, min, max, init, d -> onChange.accept(d.intValue()), true);
    }

    private MappedSlider(int x, int y, int width, int height, Text message, double min, double max, double init, Consumer<Double> onChange, boolean integer) {
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
        this.setMessage(Text.literal(originalMessage.getString() + ": " + format(v)));
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