package dev.bunar.client.render;

import com.mojang.blaze3d.opengl.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.bunar.client.BunarClient;
import dev.bunar.client.render.blur.SeparableBlur;
import dev.bunar.client.render.font.FontRenderer;
import dev.bunar.client.render.gl.GlState;
import dev.bunar.client.ui.ClickGuiPainter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import static org.lwjgl.opengl.GL33C.*;

/** PIP bridge adapted from Noamm9/NVGRenderer; see licenses/NVGRenderer.txt. */
public final class BunarGuiRenderer extends PictureInPictureRenderer<BunarRenderState> {
    private RectBatch rectangles;
    private SeparableBlur blur;
    private FontRenderer fonts;
    private final ClickGuiPainter painter = new ClickGuiPainter();
    public BunarGuiRenderer(MultiBufferSource.BufferSource buffers) { super(buffers); }
    @Override public Class<BunarRenderState> getRenderStateClass() { return BunarRenderState.class; }
    @Override protected String getTextureLabel() { return "bunar_gui"; }
    @Override protected float getTranslateY(int height, int scale) { return height / 2f; }
    @Override protected boolean textureIsReadyToBlit(BunarRenderState state) { return false; }
    @Override protected void renderToTexture(BunarRenderState state, PoseStack poses) {
        if (!(RenderSystem.getDevice() instanceof GlDevice device)) return;
        var color = RenderSystem.outputColorTextureOverride;
        var depth = RenderSystem.outputDepthTextureOverride;
        if (color == null || depth == null || !(color.texture() instanceof GlTexture output) || !(depth.texture() instanceof GlTexture depthTexture)) return;
        int w = color.getWidth(0), h = color.getHeight(0);
        if (w <= 0 || h <= 0) return;
        try (GlState ignored = new GlState()) {
            int fbo = output.getFbo(device.directStateAccess(), depthTexture);
            glBindFramebuffer(GL_FRAMEBUFFER, fbo); glViewport(0, 0, w, h);
            if (rectangles == null) { rectangles = new RectBatch(); blur = new SeparableBlur(); fonts = new FontRenderer(); }
            var kernel = BunarClient.preparation().kernelIfReady();
            var mainColor = Minecraft.getInstance().getMainRenderTarget().getColorTexture();
            if (kernel != null && mainColor instanceof GlTexture source && source != output) blur.render(source.glId(), fbo, w, h, kernel);
            rectangles.begin(state.width(), state.height());
            painter.rectangles(rectangles, state.layout(), state.selected(), state.hovered(), state.width(), state.height());
            rectangles.flush();
            fonts.begin(state.width(), state.height(), (float) w / state.width());
            try { painter.text(fonts, state.layout(), state.selected()); } finally { fonts.end(); }
        }
    }
    @Override public void close() {
        if (fonts != null) fonts.close(); if (blur != null) blur.close(); if (rectangles != null) rectangles.close();
        fonts = null; blur = null; rectangles = null; super.close();
    }
}
