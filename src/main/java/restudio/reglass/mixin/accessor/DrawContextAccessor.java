package restudio.reglass.mixin.accessor;

//? if >= 26 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.GuiGraphicsExtractor.ScissorStack;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
//? } else {
/*import net.minecraft.client.gui.DrawContext;
*///? }
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//? if >= 26 {
@Mixin(GuiGraphicsExtractor.class)
//? } else {
/*@Mixin(DrawContext.class)
*///? }
public interface DrawContextAccessor {

    @Accessor("scissorStack")
//? if >= 26 {
    ScissorStack getScissorStack();

    @Accessor("guiRenderState")
    GuiRenderState getGuiRenderState();
//? } else {
    /*DrawContext.ScissorStack getScissorStack();
*///? }
}
