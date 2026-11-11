package dev.bunar.client.render.font;

import org.lwjgl.nanovg.NVGColor;
import java.nio.ByteBuffer;
import java.io.IOException;
import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL3.*;
import static org.lwjgl.system.MemoryUtil.*;

/** One font atlas/context, persistent native color, no file IO after initialization. */
public final class FontRenderer implements AutoCloseable {
    private final long vg;
    private final NVGColor color = NVGColor.calloc();
    private final ByteBuffer fontData;
    public FontRenderer() {
        vg = nvgCreate(NVG_ANTIALIAS);
        if (vg == 0) { color.free(); throw new IllegalStateException("NanoVG initialization failed"); }
        try (var in = FontRenderer.class.getResourceAsStream("/assets/bunar/fonts/Inter-Regular.ttf")) {
            if (in == null) throw new IOException("Missing Inter font");
            byte[] bytes = in.readAllBytes(); fontData = memAlloc(bytes.length); fontData.put(bytes).flip();
        } catch (IOException e) { nvgDelete(vg); color.free(); throw new IllegalStateException(e); }
        if (nvgCreateFontMem(vg, "inter", fontData, false) == -1) {
            memFree(fontData); nvgDelete(vg); color.free(); throw new IllegalStateException("Invalid font");
        }
    }
    public void begin(float width, float height, float pixelRatio) {
        nvgBeginFrame(vg, width, height, pixelRatio); nvgFontFace(vg, "inter"); nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
    }
    public void text(String text, float x, float y, float size, int argb) {
        setColor(argb);
        nvgFillColor(vg, color); nvgFontSize(vg, size); nvgText(vg, x, y, text);
    }
    public void pushTransform(float x, float y, float scale) {
        nvgSave(vg);
        nvgTranslate(vg, x, y);
        nvgScale(vg, scale, scale);
    }
    public void popTransform() { nvgRestore(vg); }
    public void roundedRect(float x, float y, float width, float height, float radius, int argb) {
        setColor(argb);
        nvgBeginPath(vg);
        nvgRoundedRect(vg, x, y, width, height, radius);
        nvgFillColor(vg, color);
        nvgFill(vg);
    }
    public void line(float x1, float y1, float x2, float y2, float width, int argb) {
        setColor(argb);
        nvgBeginPath(vg);
        nvgMoveTo(vg, x1, y1);
        nvgLineTo(vg, x2, y2);
        nvgStrokeWidth(vg, width);
        nvgLineCap(vg, NVG_ROUND);
        nvgStrokeColor(vg, color);
        nvgStroke(vg);
    }
    private void setColor(int argb) {
        color.r(((argb >> 16) & 255) / 255f).g(((argb >> 8) & 255) / 255f)
                .b((argb & 255) / 255f).a((argb >>> 24) / 255f);
    }
    public void end() { nvgEndFrame(vg); }
    @Override public void close() { nvgDelete(vg); color.free(); memFree(fontData); }
}
