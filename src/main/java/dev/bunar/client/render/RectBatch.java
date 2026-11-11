package dev.bunar.client.render;

import dev.bunar.client.render.gl.ShaderProgram;
import java.nio.FloatBuffer;
import static org.lwjgl.opengl.GL33C.*;
import static org.lwjgl.system.MemoryUtil.*;

/** Painter-ordered quads. Six vertices/rect, one streaming upload and draw per capacity-sized batch. */
public final class RectBatch implements AutoCloseable {
    public static final int CAPACITY = 4096;
    private final FloatBuffer vertices = memAllocFloat(CAPACITY * 36);
    private final ShaderProgram shader = new ShaderProgram(ShaderProgram.resource("rect.vert"), ShaderProgram.resource("rect.frag"));
    private final int vao = glGenVertexArrays(), vbo = glGenBuffers(), viewport = shader.uniform("viewport");
    private int count, draws;
    private float width, height;
    public RectBatch() {
        glBindVertexArray(vao); glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, (long) vertices.capacity() * Float.BYTES, GL_STREAM_DRAW);
        glEnableVertexAttribArray(0); glVertexAttribPointer(0, 2, GL_FLOAT, false, 24, 0);
        glEnableVertexAttribArray(1); glVertexAttribPointer(1, 4, GL_FLOAT, false, 24, 8);
    }
    public void begin(float width, float height) {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("Invalid viewport");
        this.width = width; this.height = height; vertices.clear(); count = 0; draws = 0;
    }
    public void rect(float x, float y, float w, float h, int argb) {
        if (w <= 0 || h <= 0 || (argb >>> 24) == 0 || x >= width || y >= height || x + w <= 0 || y + h <= 0) return;
        if (count == CAPACITY) flush();
        float a = (argb >>> 24) / 255f, r = ((argb >> 16) & 255) / 255f;
        float g = ((argb >> 8) & 255) / 255f, b = (argb & 255) / 255f;
        vertex(x, y, r, g, b, a); vertex(x + w, y, r, g, b, a); vertex(x + w, y + h, r, g, b, a);
        vertex(x, y, r, g, b, a); vertex(x + w, y + h, r, g, b, a); vertex(x, y + h, r, g, b, a); count++;
    }
    private void vertex(float x, float y, float r, float g, float b, float a) { vertices.put(x).put(y).put(r).put(g).put(b).put(a); }
    public void flush() {
        if (count == 0) return;
        glDisable(GL_DEPTH_TEST); glDisable(GL_CULL_FACE); glDisable(GL_STENCIL_TEST); glDisable(GL_SCISSOR_TEST);
        glEnable(GL_BLEND); glBlendEquation(GL_FUNC_ADD); glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        glUseProgram(shader.id); glUniform2f(viewport, width, height);
        glBindVertexArray(vao); glBindBuffer(GL_ARRAY_BUFFER, vbo); vertices.flip();
        glBufferData(GL_ARRAY_BUFFER, (long) vertices.capacity() * Float.BYTES, GL_STREAM_DRAW);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertices); glDrawArrays(GL_TRIANGLES, 0, count * 6);
        vertices.clear(); count = 0; draws++;
    }
    public int drawCalls() { return draws; }
    @Override public void close() { glDeleteBuffers(vbo); glDeleteVertexArrays(vao); shader.close(); memFree(vertices); }
}
