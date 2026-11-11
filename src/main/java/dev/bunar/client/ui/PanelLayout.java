package dev.bunar.client.ui;

import dev.bunar.client.ui.GuiSettings.Option;

/** Shared geometry for NanoVG drawing and pointer input. */
public record PanelLayout(float x, float y, float scale) {
    public static final int WIDTH = 640;
    public static final int HEIGHT = 376;
    static final int RAIL = 56;
    static final int CARD_WIDTH = 266;
    static final int CARD_TOP = 114;
    static final int ROW_HEIGHT = 51;
    static final int TRACK_WIDTH = 76;
    static final Bounds RESET = new Bounds(86, 225, 221, 36);

    public static PanelLayout forWindow(int width, int height, int scalePercent) {
        float fit = Math.min((width - 16f) / WIDTH, (height - 16f) / HEIGHT);
        float scale = Math.max(0.01f, Math.min(scalePercent / 100f, fit));
        return new PanelLayout((width - WIDTH * scale) / 2, (height - HEIGHT * scale) / 2, scale);
    }

    float localX(double screenX) { return (float) (screenX - x) / scale; }
    float localY(double screenY) { return (float) (screenY - y) / scale; }

    static Bounds categoryBounds(Category category) { return new Bounds(9, 69 + category.ordinal() * 48, 38, 38); }
    static int columnX(int column) { return 72 + column * (CARD_WIDTH + 16); }
    static Bounds rowBounds(int column, int row) {
        return new Bounds(columnX(column) + 8, CARD_TOP + 43 + row * ROW_HEIGHT, CARD_WIDTH - 16, ROW_HEIGHT);
    }

    public Category categoryAt(double mouseX, double mouseY) {
        float x = localX(mouseX), y = localY(mouseY);
        for (Category category : Category.values()) {
            if (categoryBounds(category).contains(x, y)) return category;
        }
        return null;
    }

    Control controlAt(Category category, double mouseX, double mouseY) {
        float x = localX(mouseX), y = localY(mouseY);
        for (int column = 0; column < category.groups.size(); column++) {
            var options = category.groups.get(column).options();
            for (int row = 0; row < options.size(); row++) {
                Bounds bounds = rowBounds(column, row);
                if (bounds.contains(x, y)) return new Control(options.get(row), bounds);
            }
        }
        return null;
    }

    static float trackX(Bounds row) { return row.right() - TRACK_WIDTH - 10; }

    record Control(Option option, Bounds bounds) {}
    record Bounds(int x, int y, int width, int height) {
        int right() { return x + width; }
        boolean contains(float px, float py) {
            return px >= x && px < right() && py >= y && py < y + height;
        }
    }
}
