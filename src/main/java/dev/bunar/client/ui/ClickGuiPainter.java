package dev.bunar.client.ui;

import dev.bunar.client.render.RectBatch;
import dev.bunar.client.render.font.FontRenderer;

/** Stateless painter: a square shell, left navigation, empty feature area. */
public final class ClickGuiPainter {
    private static final int TEXT = 0xffedf0f7, MUTED = 0xff8b92a8, ACCENT = 0xffa49aff;
    public void rectangles(RectBatch batch, PanelLayout p, int selected, int hovered, int width, int height) {
        batch.rect(0, 0, width, height, 0x70090b11);
        rect(batch, p, 0, 0, 400, 400, 0xf5161923);
        rect(batch, p, 0, 0, 136, 400, 0xff11141d);
        rect(batch, p, 135, 0, 1, 400, 0xff272b39);
        rect(batch, p, 0, 0, 400, 1, 0xff34384b);
        rect(batch, p, 0, 399, 400, 1, 0xff272b39);
        rect(batch, p, 18, 24, 28, 28, ACCENT);
        // Original geometric B mark.
        rect(batch, p, 25, 30, 3, 16, 0xff11141d);
        rect(batch, p, 28, 30, 9, 3, 0xff11141d);
        rect(batch, p, 28, 37, 9, 3, 0xff11141d);
        rect(batch, p, 28, 43, 9, 3, 0xff11141d);
        rect(batch, p, 36, 33, 3, 4, 0xff11141d);
        rect(batch, p, 36, 40, 3, 3, 0xff11141d);
        for (int i = 0; i < Category.values().length; i++) {
            float y = 98 + i * 42;
            if (i == selected || i == hovered) rect(batch, p, 12, y, 112, 36, i == selected ? 0xff2b2944 : 0xff1c202c);
            if (i == selected) rect(batch, p, 12, y + 9, 2, 18, ACCENT);
            icon(batch, p, 24, y + 12, i, i == selected ? ACCENT : MUTED);
        }
        rect(batch, p, 154, 83, 228, 1, 0xff2b2e3d);
        rect(batch, p, 248, 165, 36, 36, 0xff252937);
        rect(batch, p, 258, 175, 16, 2, MUTED); rect(batch, p, 258, 182, 11, 2, MUTED); rect(batch, p, 258, 189, 14, 2, MUTED);
        rect(batch, p, 154, 347, 228, 1, 0xff2b2e3d);
    }
    private void icon(RectBatch b, PanelLayout p, float x, float y, int type, int c) {
        if (type == 0) { rect(b,p,x,y,12,2,c); rect(b,p,x,y+5,5,7,c); rect(b,p,x+7,y+5,5,7,c); }
        else if (type == 1) { rect(b,p,x+4,y,4,12,c); rect(b,p,x,y+4,12,4,c); }
        else if (type == 2) { rect(b,p,x+4,y,5,5,c); rect(b,p,x+1,y+7,11,5,c); }
        else { rect(b,p,x,y+1,12,2,c); rect(b,p,x,y+6,12,2,c); rect(b,p,x,y+11,12,2,c); rect(b,p,x+3,y,2,4,c); rect(b,p,x+8,y+5,2,4,c); }
    }
    private void rect(RectBatch b, PanelLayout p, float x, float y, float w, float h, int color) {
        b.rect(p.x()+x*p.scale(),p.y()+y*p.scale(),w*p.scale(),h*p.scale(),color);
    }
    public void text(FontRenderer f, PanelLayout p, int selected) {
        text(f,p,"BUNAR",54,26,16,TEXT); text(f,p,"CLIENT",55,45,8,MUTED);
        text(f,p,"WORKSPACE",18,76,8,MUTED);
        for (int i=0;i<Category.values().length;i++) text(f,p,Category.values()[i].label,46,110+i*42,12,i==selected?TEXT:MUTED);
        Category category=Category.values()[selected];
        text(f,p,category.label,154,25,23,TEXT); text(f,p,category.description,155,57,10,MUTED);
        text(f,p,"A clean starting point",200,218,12,TEXT);
        text(f,p,"Your features will appear here.",188,241,10,MUTED);
        text(f,p,"1.21.11 / FABRIC",18,365,8,MUTED);
        text(f,p,"Bunar Client",154,363,10,TEXT); text(f,p,"0.1.0",352,364,9,MUTED);
        text(f,p,"ESC to close",154,380,8,MUTED);
    }
    private void text(FontRenderer f,PanelLayout p,String s,float x,float y,float size,int c) { f.text(s,p.x()+x*p.scale(),p.y()+y*p.scale(),size*p.scale(),c); }
}
