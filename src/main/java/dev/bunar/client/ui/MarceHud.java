package dev.bunar.client.ui;

import dev.bunar.client.ui.GuiSettings.Option;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class MarceHud {
    private MarceHud() {}

    public static void render(GuiGraphics graphics, GuiSettings settings) {
        var client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui || client.screen instanceof ClickGuiScreen ||
                !settings.enabled(Option.HUD)) return;
        String fps = settings.enabled(Option.FPS) ? client.getFps() + " FPS" : "";
        String coordinates = settings.enabled(Option.COORDINATES) ?
                "XYZ  " + client.player.getBlockX() + "  " + client.player.getBlockY() + "  " + client.player.getBlockZ() : "";
        if (fps.isEmpty() && coordinates.isEmpty()) return;

        if (settings.enabled(Option.COMPACT) && !fps.isEmpty() && !coordinates.isEmpty()) {
            drawLine(graphics, client, fps + "  /  " + coordinates, 8, settings.accent());
        } else {
            if (!fps.isEmpty()) drawLine(graphics, client, fps, 8, settings.accent());
            if (!coordinates.isEmpty()) drawLine(graphics, client, coordinates, fps.isEmpty() ? 8 : 27, settings.accent());
        }
    }

    private static void drawLine(GuiGraphics graphics, Minecraft client, String text, int top, int accent) {
        graphics.fill(8, top, 22 + client.font.width(text), top + 16, 0xe415131c);
        graphics.fill(8, top, 10, top + 16, accent);
        graphics.drawString(client.font, text, 15, top + 4, 0xfff6f2ff, false);
    }
}
