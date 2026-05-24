package restudio.reglass.mixin.client;

//#if MC >= 26
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
//#else
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
//#endif
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import restudio.reglass.client.ReGlassClient;

//#if MC >= 26
@Mixin(Options.class)
//#else
@Mixin(GameOptions.class)
//#endif
public class OptionsMixin {
    @Mutable
    @Shadow
    @Final
//#if MC >= 26
    public KeyMapping[] keyMappings;
//#else
    public KeyBinding[] allKeys;
//#endif

    @Inject(
//#if MC >= 26
            method = "<init>",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;load()V")
//#else
            method = "<init>",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;load()V")
//#endif
    )
    private void reglass$appendKeyMappings(CallbackInfo ci) {
//#if MC >= 26
        keyMappings = ReGlassClient.appendKeyMappings(keyMappings);
//#else
        allKeys = ReGlassClient.appendKeyMappings(allKeys);
//#endif
    }
}
