package restudio.reglass.mixin.accessor;

//? if >= 26 {
import net.minecraft.client.renderer.state.gui.GuiRenderState;
//? } else {
/*import net.minecraft.client.gui.render.state.GuiRenderState;
*///? }
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiRenderState.class)
public interface GuiRenderStateAccessor {
    @Accessor("blurLayer")
    int getBlurLayer();
}
