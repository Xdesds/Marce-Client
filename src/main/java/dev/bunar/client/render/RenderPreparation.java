package dev.bunar.client.render;

import dev.bunar.client.render.blur.BlurKernel;
import java.util.concurrent.*;

/** CPU preparation only. One cached request, no per-frame jobs or unbounded task queue. */
public final class RenderPreparation implements AutoCloseable {
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "bunar-render-prepare"); t.setDaemon(true); return t;
    });
    private final CompletableFuture<BlurKernel> kernel = CompletableFuture.supplyAsync(() -> BlurKernel.gaussian(3f), worker);
    public BlurKernel kernelIfReady() { return kernel.getNow(null); }
    @Override public void close() { worker.shutdownNow(); }
}
