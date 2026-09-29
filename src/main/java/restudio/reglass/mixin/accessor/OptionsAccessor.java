package restudio.reglass.mixin.accessor;

//#if MC >= 26
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
//#else
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
//#endif
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

//#if MC >= 26
@Mixin(Options.class)
//#else
@Mixin(GameOptions.class)
//#endif
public interface OptionsAccessor {
    @Mutable
//#if MC >= 26
    @Accessor("keyMappings")
    void setKeyMappings(KeyMapping[] keyMappings);
//#else
    @Accessor("allKeys")
    void setKeyMappings(KeyBinding[] keyMappings);
//#endif
}

