package dev.bunar.client.ui;

import dev.bunar.client.render.font.FontRenderer;
import dev.bunar.client.ui.GuiSettings.Option;
import dev.bunar.client.ui.PanelLayout.Bounds;

/** Draws the same controls and bounds used by the input handler. */
public final class ClickGuiPainter {
    private static final int BORDER = 0xff32303c;
    private static final int SHELL = 0xff111116;
    private static final int CARD = 0xff19181f;
    private static final int HOVER = 0xff24212e;
    private static final int TEXT = 0xfff3f0f9;
    private static final int MUTED = 0xffada6ba;
    private static final int TRACK = 0xff363240;

    public void draw(FontRenderer canvas, PanelLayout layout, Category selected, GuiSettings settings,
                     int mouseX, int mouseY) {
        canvas.pushTransform(layout.x(), layout.y(), layout.scale());
        try {
            int accent = settings.accent();
            canvas.roundedRect(0, 0, PanelLayout.WIDTH, PanelLayout.HEIGHT, 15, BORDER);
            canvas.roundedRect(1, 1, PanelLayout.WIDTH - 2, PanelLayout.HEIGHT - 2, 14, SHELL);
            canvas.roundedRect(6, 6, 44, PanelLayout.HEIGHT - 12, 11, CARD);
            canvas.roundedRect(12, 17, 32, 32, 9, accent);
            drawPlanet(canvas);

            Category hovered = layout.categoryAt(mouseX, mouseY);
            for (Category category : Category.values()) {
                Bounds bounds = PanelLayout.categoryBounds(category);
                if (category == selected || category == hovered) fill(canvas, bounds, 9, HOVER);
                if (category == selected) canvas.roundedRect(10, bounds.y() + 12, 2, 14, 1, accent);
                drawCategory(canvas, category, bounds.x() + 11, bounds.y() + 11, category == selected ? accent : MUTED);
            }

            canvas.text("MARCE CLIENT", 73, 20, 10, accent);
            canvas.text(selected.label, 72, 39, 29, TEXT);
            canvas.text(selected.description, 73, 77, 12, MUTED);
            canvas.roundedRect(566, 23, 52, 24, 6, CARD);
            canvas.text("ESC", 579, 28, 11, MUTED);

            var hoveredControl = layout.controlAt(selected, mouseX, mouseY);
            for (int column = 0; column < selected.groups.size(); column++) {
                drawGroup(canvas, selected.groups.get(column), column, hoveredControl, settings);
            }
            if (selected == Category.SETTINGS) {
                boolean resetHovered = PanelLayout.RESET.contains(layout.localX(mouseX), layout.localY(mouseY));
                fill(canvas, PanelLayout.RESET, 7, resetHovered ? HOVER : SHELL);
                canvas.text("Restore defaults", PanelLayout.RESET.x() + 12, PanelLayout.RESET.y() + 10, 13, accent);
                canvas.text("Switch category", 369, 171, 13, TEXT);
                canvas.text("1 - 3", 564, 171, 13, MUTED);
                canvas.text("Close menu", 369, 222, 13, TEXT);
                canvas.text("Esc", 564, 222, 13, MUTED);
            }

            if (settings.enabled(Option.HELP)) {
                String help = hoveredControl == null ? "" : hoveredControl.option().description;
                if (hovered != null) help = hovered.label + "  /  " + hovered.description;
                if (!help.isEmpty()) {
                    canvas.roundedRect(72, 307, 548, 38, 8, CARD);
                    canvas.roundedRect(84, 324, 5, 5, 3, accent);
                    canvas.text(help, 98, 319, 12, MUTED);
                }
            }
        } finally {
            canvas.popTransform();
        }
    }

    private void drawGroup(FontRenderer canvas, Category.Group group, int column,
                           PanelLayout.Control hovered, GuiSettings settings) {
        int x = PanelLayout.columnX(column);
        canvas.roundedRect(x, PanelLayout.CARD_TOP, PanelLayout.CARD_WIDTH, 171, 11, CARD);
        canvas.text(group.title(), x + 16, PanelLayout.CARD_TOP + 14, 14, MUTED);
        for (int i = 0; i < group.options().size(); i++) {
            Option option = group.options().get(i);
            Bounds bounds = PanelLayout.rowBounds(column, i);
            if (hovered != null && hovered.option() == option) fill(canvas, bounds, 7, HOVER);
            if (i > 0) canvas.roundedRect(x + 16, bounds.y(), PanelLayout.CARD_WIDTH - 32, 1, 0, BORDER);
            canvas.text(option.label, bounds.x() + 8, bounds.y() + 17, 13, TEXT);
            drawValue(canvas, option, bounds, settings);
        }
    }

    private void drawValue(FontRenderer canvas, Option option, Bounds bounds, GuiSettings settings) {
        int y = bounds.y() + 19;
        int right = bounds.right() - 10;
        int accent = settings.accent();
        if (option.slider()) {
            float x = PanelLayout.trackX(bounds);
            float progress = settings.get(option) / (float) option.max;
            int percent = option == Option.SCALE ? settings.scalePercent() : settings.get(option);
            canvas.text(percent + "%", x + 44, y - 12, 10, MUTED);
            canvas.roundedRect(x, y + 10, PanelLayout.TRACK_WIDTH, 4, 2, TRACK);
            if (progress > 0) canvas.roundedRect(x, y + 10, PanelLayout.TRACK_WIDTH * progress, 4, 2, accent);
            canvas.roundedRect(x + PanelLayout.TRACK_WIDTH * progress - 4, y + 8, 8, 8, 4, TEXT);
        } else if (option == Option.ACCENT) {
            canvas.text(settings.palette(), right - 63, y, 11, MUTED);
            canvas.roundedRect(right - 13, y, 13, 13, 4, accent);
        } else {
            boolean on = settings.enabled(option);
            canvas.roundedRect(right - 30, y, 30, 15, 8, on ? accent : TRACK);
            canvas.roundedRect(right - (on ? 13 : 27), y + 3, 9, 9, 5, on ? 0xff23172c : MUTED);
        }
    }

    private static void drawPlanet(FontRenderer canvas) {
        canvas.roundedRect(20, 23, 16, 16, 8, 0xff2c203a);
        canvas.line(18, 36, 38, 27, 2.7f, 0xff2c203a);
        canvas.line(21, 34, 35, 28, 1.3f, 0xffead8fc);
    }

    private static void drawCategory(FontRenderer canvas, Category category, int x, int y, int color) {
        switch (category) {
            case VISUAL -> {
                canvas.roundedRect(x + 1, y + 3, 14, 10, 5, color);
                canvas.roundedRect(x + 6, y + 6, 4, 4, 2, SHELL);
            }
            case HUD -> {
                canvas.roundedRect(x + 1, y + 2, 14, 10, 2, color);
                canvas.roundedRect(x + 3, y + 4, 10, 6, 1, SHELL);
                canvas.line(x + 5, y + 14, x + 11, y + 14, 1.5f, color);
            }
            case SETTINGS -> {
                for (int i = 0; i < 3; i++) {
                    int lineY = y + 3 + i * 5;
                    canvas.line(x + 2, lineY, x + 14, lineY, 1.3f, color);
                    canvas.roundedRect(x + (i % 2 == 0 ? 5 : 9), lineY - 2, 4, 4, 2, color);
                }
            }
        }
    }

    private static void fill(FontRenderer canvas, Bounds bounds, int radius, int color) {
        canvas.roundedRect(bounds.x(), bounds.y(), bounds.width(), bounds.height(), radius, color);
    }
}
