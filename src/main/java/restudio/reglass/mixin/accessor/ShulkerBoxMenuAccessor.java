package restudio.reglass.mixin.accessor;

//? if >= 26 {
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ShulkerBoxMenu;
//? } else {
/*import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ShulkerBoxScreenHandler;
*///? }
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//? if >= 26 {
@Mixin(ShulkerBoxMenu.class)
//? } else {
/*@Mixin(ShulkerBoxScreenHandler.class)
*///? }
public interface ShulkerBoxMenuAccessor {
//? if >= 26 {
    @Accessor("container")
    Container reglass$getContainer();
//? } else {
    /*@Accessor("inventory")
    Inventory reglass$getInventory();
*///? }
}

