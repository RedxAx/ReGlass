package restudio.reglass.mixin.accessor;

//? if >= 26 {
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
//? } else {
/*import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
*///? }
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

//? if >= 26 {
@Mixin(Options.class)
//? } else {
/*@Mixin(GameOptions.class)
*///? }
public interface OptionsAccessor {
    @Mutable
//? if >= 26 {
    @Accessor("keyMappings")
    void setKeyMappings(KeyMapping[] keyMappings);
//? } else {
    /*@Accessor("allKeys")
    void setKeyMappings(KeyBinding[] keyMappings);
*///? }
}

