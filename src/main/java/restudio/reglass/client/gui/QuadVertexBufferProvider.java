package restudio.reglass.client.gui;

//#if MC >= 26.3
import com.mojang.renderpearl.api.buffers.GpuBuffer;
//#else
import com.mojang.blaze3d.buffers.GpuBuffer;
//#endif

public interface QuadVertexBufferProvider {
    GpuBuffer getQuadVertexBuffer();
}
