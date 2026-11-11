package dev.bunar.client;

import dev.bunar.client.render.*;
import dev.bunar.client.render.blur.*;
import dev.bunar.client.render.font.FontRenderer;
import dev.bunar.client.render.gl.GlState;
import dev.bunar.client.ui.*;
import org.junit.jupiter.api.*;
import org.lwjgl.opengl.GL;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.nio.ByteBuffer;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33C.*;
import static org.lwjgl.system.MemoryUtil.*;

@Tag("gpu")
class GpuTest {
    @Test void actualOpenGlCorrectnessAndBenchmark() throws Exception {
        assertTrue(glfwInit(), "GLFW initialization (a desktop GPU session is required)");
        glfwWindowHint(GLFW_VISIBLE,GLFW_FALSE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR,3); glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR,3);
        glfwWindowHint(GLFW_OPENGL_PROFILE,GLFW_OPENGL_CORE_PROFILE); glfwWindowHint(GLFW_STENCIL_BITS,8);
        long window=glfwCreateWindow(1280,960,"Bunar renderer verification",0,0);
        assertNotEquals(0,window);
        glfwMakeContextCurrent(window); GL.createCapabilities();
        String gpu=glGetString(GL_RENDERER), version=glGetString(GL_VERSION);
        Path reports=Path.of("build/reports/bunar"); Files.createDirectories(reports);
        try(var guard=new GlState(); var batch=new RectBatch(); var blur=new SeparableBlur(); var fonts=new FontRenderer()) {
            glEnable(GL_SCISSOR_TEST); glScissor(3,4,9,10); glStencilMaskSeparate(GL_BACK,13);
            try(var state=new GlState()) { glDisable(GL_SCISSOR_TEST); glScissor(0,0,1,1); glStencilMaskSeparate(GL_BACK,0); glPixelStorei(GL_UNPACK_ALIGNMENT,1); }
            assertTrue(glIsEnabled(GL_SCISSOR_TEST)); assertEquals(13,glGetInteger(GL_STENCIL_BACK_WRITEMASK));
            int[] box=new int[4]; glGetIntegerv(GL_SCISSOR_BOX,box); assertArrayEquals(new int[]{3,4,9,10},box);
            assertEquals(4,glGetInteger(GL_UNPACK_ALIGNMENT)); glDisable(GL_SCISSOR_TEST); glStencilMask(~0);
            glViewport(0,0,1280,960);
            // Correctness: transparent painter order and off-screen/empty culling.
            glClearColor(0,0,0,1); glClear(GL_COLOR_BUFFER_BIT|GL_STENCIL_BUFFER_BIT);
            batch.begin(1280,960);
            batch.rect(0,0,1280,960,0xffff0000); batch.rect(0,0,1280,960,0x800000ff);
            batch.rect(-100,-100,1,1,0xffffffff); batch.rect(0,0,0,3,0xffffffff);
            batch.flush(); assertEquals(1,batch.drawCalls());
            byte[] pixel=readPixel(640,480);
            assertEquals(127,Byte.toUnsignedInt(pixel[0]),2); assertEquals(128,Byte.toUnsignedInt(pixel[2]),2);
            batch.begin(1280,960);
            for(int i=0;i<RectBatch.CAPACITY+1;i++) batch.rect(1,1,2,2,0xff223344);
            batch.flush(); assertEquals(2,batch.drawCalls());

            int texture=glGenTextures(); glBindTexture(GL_TEXTURE_2D,texture);
            glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_LINEAR); glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_CLAMP_TO_EDGE); glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_CLAMP_TO_EDGE);
            ByteBuffer data=memAlloc(1280*960*4);
            try {
                // Constant image must survive both normalized blur passes, including edges.
                for(int i=0;i<1280*960;i++) data.put((byte)64).put((byte)128).put((byte)192).put((byte)255);
                data.flip(); glTexImage2D(GL_TEXTURE_2D,0,GL_RGBA8,1280,960,0,GL_RGBA,GL_UNSIGNED_BYTE,data);
                blur.render(texture,0,1280,960,BlurKernel.gaussian(3));
                for(int[] point:new int[][]{{0,0},{640,480},{1279,959}}) {
                    pixel=readPixel(point[0],point[1]);
                    assertEquals(64,Byte.toUnsignedInt(pixel[0]),2); assertEquals(128,Byte.toUnsignedInt(pixel[1]),2); assertEquals(192,Byte.toUnsignedInt(pixel[2]),2);
                }
                blur.render(texture,0,1280,960,BlurKernel.gaussian(3)); assertEquals(1,blur.allocations());
                // An isolated square spreads on both axes, proving horizontal and vertical filtering.
                data.clear();
                for(int y=0;y<960;y++) for(int x=0;x<1280;x++) {
                    byte c=(byte)((x>=630 && x<650 && y>=470 && y<490)?255:0); data.put(c).put(c).put(c).put((byte)255);
                }
                data.flip(); glBindTexture(GL_TEXTURE_2D,texture); glTexSubImage2D(GL_TEXTURE_2D,0,0,0,1280,960,GL_RGBA,GL_UNSIGNED_BYTE,data);
                blur.render(texture,0,1280,960,BlurKernel.gaussian(3));
                assertTrue(Byte.toUnsignedInt(readPixel(625,480)[0])>0,"Horizontal spreading");
                assertTrue(Byte.toUnsignedInt(readPixel(640,465)[0])>0,"Vertical spreading");
                blur.render(texture,0,640,480,BlurKernel.gaussian(3)); assertEquals(2,blur.allocations());
                blur.render(texture,0,1280,960,BlurKernel.gaussian(3)); assertEquals(3,blur.allocations());

                // Synthetic scene for the UI screenshot, not a claim of in-game verification.
                data.clear();
                for(int y=0;y<960;y++) for(int x=0;x<1280;x++) {
                    data.put((byte)(25+x/14)).put((byte)(35+y/13)).put((byte)(65+(x/80+y/80)%2*40)).put((byte)255);
                }
                data.flip(); glBindTexture(GL_TEXTURE_2D,texture); glTexSubImage2D(GL_TEXTURE_2D,0,0,0,1280,960,GL_RGBA,GL_UNSIGNED_BYTE,data);
                blur.render(texture,0,1280,960,BlurKernel.gaussian(3));
                var painter=new ClickGuiPainter(); var layout=PanelLayout.forWindow(640,480,100);
                batch.begin(640,480); batch.rect(0,0,640,480,0x73090810); batch.flush();
                fonts.begin(640,480,2);
                painter.draw(fonts,layout,Category.VISUAL,GuiSettings.DEFAULT,180,220);
                fonts.end();
                saveScreenshot(reports.resolve("clickgui-preview.png"));
            } finally { memFree(data); glDeleteTextures(texture); }
            assertEquals(GL_NO_ERROR,glGetError(),"No OpenGL errors after renderer and font operations");

            for(int i=0;i<10;i++) { rectangles(batch,false); rectangles(batch,true); }
            double batched=measure(batch,false), immediate=measure(batch,true);
            String report="GPU: "+gpu+"\nOpenGL: "+version+"\n1024 visible opaque rectangles, 20 frames; wall time includes glFinish.\nBatched: "+batched+" ms/frame; 1 draw/frame\nFlush per rect: "+immediate+" ms/frame; 1024 draws/frame\nRatio: "+immediate/batched+"x\nThese are synthetic measurements, not Minecraft FPS.\n";
            Files.writeString(reports.resolve("gpu-benchmark.txt"),report); System.out.println(report);
            assertEquals(GL_NO_ERROR,glGetError());
        } finally { glfwDestroyWindow(window); glfwTerminate(); }
    }
    private static void rectangles(RectBatch b,boolean immediate) {
        b.begin(1280,960);
        for(int i=0;i<1024;i++) { b.rect((i%32)*38,(i/32)*28,24,20,0xffa49aff); if(immediate)b.flush(); }
        b.flush(); glFinish();
    }
    private static double measure(RectBatch b,boolean immediate) {
        long start=System.nanoTime(); for(int i=0;i<20;i++)rectangles(b,immediate); return (System.nanoTime()-start)/20_000_000.0;
    }
    private static byte[] readPixel(int x,int y) {
        ByteBuffer b=memAlloc(4);
        try { glReadPixels(x,y,1,1,GL_RGBA,GL_UNSIGNED_BYTE,b); byte[] a=new byte[4]; b.get(a); return a; } finally {memFree(b);}
    }
    private static void saveScreenshot(Path file) throws Exception {
        ByteBuffer pixels=memAlloc(1280*960*4);
        try {
            glReadPixels(0,0,1280,960,GL_RGBA,GL_UNSIGNED_BYTE,pixels);
            BufferedImage image=new BufferedImage(1280,960,BufferedImage.TYPE_INT_ARGB);
            for(int y=0;y<960;y++)for(int x=0;x<1280;x++) { int i=(y*1280+x)*4; image.setRGB(x,959-y,0xff000000|Byte.toUnsignedInt(pixels.get(i))<<16|Byte.toUnsignedInt(pixels.get(i+1))<<8|Byte.toUnsignedInt(pixels.get(i+2))); }
            ImageIO.write(image,"png",file.toFile());
        } finally {memFree(pixels);}
    }
}
