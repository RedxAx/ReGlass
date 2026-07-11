package restudio.reglass.mixin.accessor;

//? if < 26 {
/*import net.minecraft.client.gui.screen.ingame.BeaconScreen;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
*///? }
import org.spongepowered.asm.mixin.Mixin;
//? if >= 26 {
import org.spongepowered.asm.mixin.gen.Invoker;
//? } else {
/*import org.spongepowered.asm.mixin.gen.Accessor;
*///? }

//? if >= 26 {
@Mixin(targets = "net.minecraft.client.gui.screens.inventory.BeaconScreen$BeaconScreenButton")
//? } else {
/*@Mixin(BeaconScreen.class)
*///? }
public interface BeaconScreenButtonAccessor {
//? if >= 26 {
    @Invoker("isSelected")
    boolean reglass$isSelected();
//? } else {
    /*@Accessor("primaryEffect")
    RegistryEntry<StatusEffect> reglass$getPrimaryEffect();

    @Accessor("secondaryEffect")
    RegistryEntry<StatusEffect> reglass$getSecondaryEffect();
*///? }
}
