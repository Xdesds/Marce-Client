package dev.bunar.client.render.blur;

import dev.bunar.client.render.gl.ShaderProgram;
import static org.lwjgl.opengl.GL33C.*;

/** Half-resolution ping-pong blur. All calls belong to the current render context. */
public final class SeparableBlur implements AutoCloseable {
    private final ShaderProgram blur = new ShaderProgram(ShaderProgram.resource("fullscreen.vert"), ShaderProgram.resource("blur.frag"));
    private final ShaderProgram copy = new ShaderProgram(ShaderProgram.resource("fullscreen.vert"), ShaderProgram.resource("copy.frag"));
    private final int direction = blur.uniform("direction"), weights = blur.uniform("weights");
    private final int vao = glGenVertexArrays();
    private final int[] textures = new int[2], fbos = new int[2];
    private int width, height, allocations;
    private BlurKernel uploadedKernel;
    public SeparableBlur() {
        glUseProgram(blur.id); glUniform1i(blur.uniform("source"), 0);
        glUseProgram(copy.id); glUniform1i(copy.uniform("source"), 0);
    }
    private void resize(int w, int h) {
        w = Math.max(1, w / 2); h = Math.max(1, h / 2);
        if (w == width && h == height) return;
        releaseTargets(); width = w; height = h;
        for (int i = 0; i < 2; i++) {
            textures[i] = glGenTextures(); glBindTexture(GL_TEXTURE_2D, textures[i]);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, w, h, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0L);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR); glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE); glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            fbos[i] = glGenFramebuffers(); glBindFramebuffer(GL_FRAMEBUFFER, fbos[i]);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textures[i], 0);
            if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) throw new IllegalStateException("Incomplete blur framebuffer");
        }
        allocations++;
    }
    public void render(int sourceTexture, int targetFbo, int fullWidth, int fullHeight, BlurKernel kernel) {
        if (fullWidth <= 0 || fullHeight <= 0) return;
        glActiveTexture(GL_TEXTURE0); glBindSampler(0, 0); resize(fullWidth, fullHeight);
        glDisable(GL_BLEND); glDisable(GL_DEPTH_TEST); glDisable(GL_CULL_FACE); glDisable(GL_SCISSOR_TEST); glDisable(GL_STENCIL_TEST);
        glBindVertexArray(vao); glUseProgram(blur.id);
        if (uploadedKernel != kernel) { glUniform1fv(weights, kernel.weights()); uploadedKernel = kernel; }
        glViewport(0, 0, width, height);
        glBindFramebuffer(GL_FRAMEBUFFER, fbos[0]); glBindTexture(GL_TEXTURE_2D, sourceTexture);
        glUniform2f(direction, 1f / width, 0); glDrawArrays(GL_TRIANGLES, 0, 3);
        glBindFramebuffer(GL_FRAMEBUFFER, fbos[1]); glBindTexture(GL_TEXTURE_2D, textures[0]);
        glUniform2f(direction, 0, 1f / height); glDrawArrays(GL_TRIANGLES, 0, 3);
        glBindFramebuffer(GL_FRAMEBUFFER, targetFbo); glViewport(0, 0, fullWidth, fullHeight);
        glUseProgram(copy.id); glBindTexture(GL_TEXTURE_2D, textures[1]); glDrawArrays(GL_TRIANGLES, 0, 3);
    }
    public int allocations() { return allocations; }
    private void releaseTargets() {
        for (int i = 0; i < 2; i++) {
            if (fbos[i] != 0) glDeleteFramebuffers(fbos[i]); if (textures[i] != 0) glDeleteTextures(textures[i]);
            fbos[i] = 0; textures[i] = 0;
        }
    }
    @Override public void close() { releaseTargets(); glDeleteVertexArrays(vao); blur.close(); copy.close(); }
}
