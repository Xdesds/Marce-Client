package dev.bunar.client;

import dev.bunar.client.render.RenderPreparation;
import dev.bunar.client.render.blur.BlurKernel;
import dev.bunar.client.ui.PanelLayout;
import dev.bunar.client.ui.Category;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CoreTest {
    @Test void gaussianIsNormalizedAndSymmetricByConstruction() {
        float[] w=BlurKernel.gaussian(3).weights();
        float sum=w[0];
        for(int i=1;i<w.length;i++) { assertTrue(w[i]>0 && w[i]<=w[i-1]); sum+=2*w[i]; }
        assertEquals(1,sum,0.000001);
        w[0]=20;
        assertTrue(BlurKernel.gaussian(3).weights()[0]<1);
        assertThrows(IllegalArgumentException.class,()->BlurKernel.gaussian(Float.NaN));
        assertThrows(IllegalArgumentException.class,()->BlurKernel.gaussian(0));
    }
    @Test void categoryHitTestsFollowWindowScaling() {
        for(int[] size:new int[][]{{1920,1080},{640,480},{320,240},{80,60}}) {
            var p=PanelLayout.forWindow(size[0],size[1],100);
            for (Category category : Category.values()) {
                assertEquals(category,p.categoryAt(p.x()+28*p.scale(),p.y()+(88+category.ordinal()*48)*p.scale()));
            }
            assertNull(p.categoryAt(p.x()+150*p.scale(),p.y()+110*p.scale()));
            assertTrue(p.x()>=0 && p.y()>=0);
        }
    }
    @Test void workerPreparesAndPublishesKernel() throws Exception {
        try(var preparation=new RenderPreparation()) {
            long deadline=System.nanoTime()+2_000_000_000L;
            while(preparation.kernelIfReady()==null && System.nanoTime()<deadline) Thread.sleep(1);
            assertNotNull(preparation.kernelIfReady());
        }
    }
}
