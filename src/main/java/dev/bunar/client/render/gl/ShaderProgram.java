package dev.bunar.client.render.gl;

import static org.lwjgl.opengl.GL33C.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class ShaderProgram implements AutoCloseable {
    public final int id;
    public ShaderProgram(String vertex, String fragment) {
        int vs = compile(GL_VERTEX_SHADER, vertex), fs;
        try { fs = compile(GL_FRAGMENT_SHADER, fragment); }
        catch (RuntimeException e) { glDeleteShader(vs); throw e; }
        id = glCreateProgram();
        glAttachShader(id, vs); glAttachShader(id, fs); glLinkProgram(id);
        glDeleteShader(vs); glDeleteShader(fs);
        if (glGetProgrami(id, GL_LINK_STATUS) == GL_FALSE) {
            String log = glGetProgramInfoLog(id); glDeleteProgram(id); throw new IllegalStateException(log);
        }
    }
    private static int compile(int type, String source) {
        int shader = glCreateShader(type); glShaderSource(shader, source); glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            String log = glGetShaderInfoLog(shader); glDeleteShader(shader); throw new IllegalStateException(log);
        }
        return shader;
    }
    public static String resource(String name) {
        try (var stream = ShaderProgram.class.getResourceAsStream("/assets/bunar/shaders/" + name)) {
            if (stream == null) throw new IllegalStateException("Missing shader: " + name);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) { throw new IllegalStateException(e); }
    }
    public int uniform(String name) { return glGetUniformLocation(id, name); }
    @Override public void close() { glDeleteProgram(id); }
}
