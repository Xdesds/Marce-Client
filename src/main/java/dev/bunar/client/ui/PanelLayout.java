package dev.bunar.client.ui;

/** One coordinate system shared by rendering and hit testing; scales down on small windows. */
public record PanelLayout(float x, float y, float scale) {
    public static final int SIZE = 400;
    public static PanelLayout forWindow(int width, int height) {
        float scale = Math.max(0.01f, Math.min(1f, Math.min((width - 16f) / SIZE, (height - 16f) / SIZE)));
        return new PanelLayout((width - SIZE * scale) / 2, (height - SIZE * scale) / 2, scale);
    }
    public float localX(double screenX) { return (float) (screenX - x) / scale; }
    public float localY(double screenY) { return (float) (screenY - y) / scale; }
    public int categoryAt(double screenX, double screenY) {
        float lx = localX(screenX), ly = localY(screenY);
        if (lx < 12 || lx >= 124) return -1;
        for (int i = 0; i < Category.values().length; i++) {
            if (ly >= 98 + i * 42 && ly < 134 + i * 42) return i;
        }
        return -1;
    }
}
