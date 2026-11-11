package dev.bunar.client.render.gl;

import static org.lwjgl.opengl.GL33C.*;

/** Guards the Minecraft GL state and keeps raw GL changes invisible to its state cache. */
public final class GlState implements AutoCloseable {
    private final int program = glGetInteger(GL_CURRENT_PROGRAM), vao = glGetInteger(GL_VERTEX_ARRAY_BINDING);
    private final int array = glGetInteger(GL_ARRAY_BUFFER_BINDING), active = glGetInteger(GL_ACTIVE_TEXTURE);
    private final int draw = glGetInteger(GL_DRAW_FRAMEBUFFER_BINDING), read = glGetInteger(GL_READ_FRAMEBUFFER_BINDING);
    private final int srcRgb = glGetInteger(GL_BLEND_SRC_RGB), dstRgb = glGetInteger(GL_BLEND_DST_RGB);
    private final int srcAlpha = glGetInteger(GL_BLEND_SRC_ALPHA), dstAlpha = glGetInteger(GL_BLEND_DST_ALPHA);
    private final int eqRgb = glGetInteger(GL_BLEND_EQUATION_RGB), eqAlpha = glGetInteger(GL_BLEND_EQUATION_ALPHA);
    private final boolean blend = glIsEnabled(GL_BLEND), depth = glIsEnabled(GL_DEPTH_TEST), cull = glIsEnabled(GL_CULL_FACE);
    private final boolean scissor = glIsEnabled(GL_SCISSOR_TEST), stencil = glIsEnabled(GL_STENCIL_TEST);
    private final boolean depthMask = glGetBoolean(GL_DEPTH_WRITEMASK);
    private final int[] viewport = new int[4], scissorBox = new int[4];
    private final int[] colorMask = new int[4];
    private final int unpackAlignment = glGetInteger(GL_UNPACK_ALIGNMENT), unpackRow = glGetInteger(GL_UNPACK_ROW_LENGTH);
    private final int unpackSkipPixels = glGetInteger(GL_UNPACK_SKIP_PIXELS), unpackSkipRows = glGetInteger(GL_UNPACK_SKIP_ROWS);
    private final int[] front = stencil(false), back = stencil(true);
    private final int texture, sampler;
    public GlState() {
        glGetIntegerv(GL_VIEWPORT, viewport); glGetIntegerv(GL_SCISSOR_BOX, scissorBox);
        glGetIntegerv(GL_COLOR_WRITEMASK, colorMask);
        glActiveTexture(GL_TEXTURE0); texture = glGetInteger(GL_TEXTURE_BINDING_2D); sampler = glGetInteger(GL_SAMPLER_BINDING);
    }
    private static void set(int cap, boolean enabled) { if (enabled) glEnable(cap); else glDisable(cap); }
    private static int[] stencil(boolean back) {
        return new int[] { glGetInteger(back?GL_STENCIL_BACK_FUNC:GL_STENCIL_FUNC),
                glGetInteger(back?GL_STENCIL_BACK_REF:GL_STENCIL_REF), glGetInteger(back?GL_STENCIL_BACK_VALUE_MASK:GL_STENCIL_VALUE_MASK),
                glGetInteger(back?GL_STENCIL_BACK_WRITEMASK:GL_STENCIL_WRITEMASK), glGetInteger(back?GL_STENCIL_BACK_FAIL:GL_STENCIL_FAIL),
                glGetInteger(back?GL_STENCIL_BACK_PASS_DEPTH_FAIL:GL_STENCIL_PASS_DEPTH_FAIL), glGetInteger(back?GL_STENCIL_BACK_PASS_DEPTH_PASS:GL_STENCIL_PASS_DEPTH_PASS) };
    }
    private static void restoreStencil(int face,int[] state) {
        glStencilFuncSeparate(face,state[0],state[1],state[2]); glStencilMaskSeparate(face,state[3]); glStencilOpSeparate(face,state[4],state[5],state[6]);
    }
    @Override public void close() {
        glUseProgram(program); glBindVertexArray(vao); glBindBuffer(GL_ARRAY_BUFFER, array);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, draw); glBindFramebuffer(GL_READ_FRAMEBUFFER, read);
        glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
        glScissor(scissorBox[0], scissorBox[1], scissorBox[2], scissorBox[3]);
        glActiveTexture(GL_TEXTURE0); glBindTexture(GL_TEXTURE_2D, texture); glBindSampler(0, sampler); glActiveTexture(active);
        glBlendFuncSeparate(srcRgb, dstRgb, srcAlpha, dstAlpha); glBlendEquationSeparate(eqRgb, eqAlpha);
        glDepthMask(depthMask);
        glColorMask(colorMask[0]!=0,colorMask[1]!=0,colorMask[2]!=0,colorMask[3]!=0);
        glPixelStorei(GL_UNPACK_ALIGNMENT,unpackAlignment); glPixelStorei(GL_UNPACK_ROW_LENGTH,unpackRow);
        glPixelStorei(GL_UNPACK_SKIP_PIXELS,unpackSkipPixels); glPixelStorei(GL_UNPACK_SKIP_ROWS,unpackSkipRows);
        restoreStencil(GL_FRONT,front); restoreStencil(GL_BACK,back);
        set(GL_BLEND, blend); set(GL_DEPTH_TEST, depth); set(GL_CULL_FACE, cull); set(GL_SCISSOR_TEST, scissor); set(GL_STENCIL_TEST, stencil);
    }
}
