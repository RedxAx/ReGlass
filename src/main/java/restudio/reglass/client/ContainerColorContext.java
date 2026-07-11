package restudio.reglass.client;

//? if >= 26 {
import net.minecraft.world.item.DyeColor;
//? } else {
/*import net.minecraft.util.DyeColor;
*///? }

public final class ContainerColorContext {
    private static DyeColor lastShulkerColor;

    private ContainerColorContext() {
    }

    public static void setLastShulkerColor(DyeColor color) {
        lastShulkerColor = color;
    }

    public static DyeColor getLastShulkerColor() {
        return lastShulkerColor;
    }
}

