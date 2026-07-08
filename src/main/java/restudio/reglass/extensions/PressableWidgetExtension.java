package restudio.reglass.extensions;

import org.spongepowered.asm.mixin.Unique;

public interface PressableWidgetExtension {
    @Unique
    boolean reglass$isDragging();
    @Unique
    void reglass$setIsDragging(boolean isDragging);
}
