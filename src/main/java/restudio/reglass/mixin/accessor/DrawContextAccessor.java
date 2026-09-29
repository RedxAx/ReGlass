package restudio.reglass.mixin.accessor;

//#if MC >= 26
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.GuiGraphicsExtractor.ScissorStack;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
//#else
import net.minecraft.client.gui.DrawContext;
//#endif
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//#if MC >= 26
@Mixin(GuiGraphicsExtractor.class)
//#else
@Mixin(DrawContext.class)
//#endif
public interface DrawContextAccessor {

    @Accessor("scissorStack")
//#if MC >= 26
    ScissorStack getScissorStack();

    @Accessor("guiRenderState")
    GuiRenderState getGuiRenderState();
//#else
    DrawContext.ScissorStack getScissorStack();
//#endif
}
