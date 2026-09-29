package restudio.reglass.mixin.accessor;

//#if MC >= 26
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ShulkerBoxMenu;
//#else
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ShulkerBoxScreenHandler;
//#endif
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//#if MC >= 26
@Mixin(ShulkerBoxMenu.class)
//#else
@Mixin(ShulkerBoxScreenHandler.class)
//#endif
public interface ShulkerBoxMenuAccessor {
//#if MC >= 26
    @Accessor("container")
    Container reglass$getContainer();
//#else
    @Accessor("inventory")
    Inventory reglass$getInventory();
//#endif
}

