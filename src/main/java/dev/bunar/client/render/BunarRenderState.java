package dev.bunar.client.render;

import dev.bunar.client.ui.Category;
import dev.bunar.client.ui.GuiSettings;
import dev.bunar.client.ui.PanelLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;

/** The deferred renderer only reads this immutable extraction snapshot. */
public record BunarRenderState(int width, int height, PanelLayout layout, Category category,
                               GuiSettings settings, int mouseX, int mouseY) implements PictureInPictureRenderState {
    @Override public int x0() { return 0; }
    @Override public int y0() { return 0; }
    @Override public int x1() { return width; }
    @Override public int y1() { return height; }
    @Override public float scale() { return 1; }
    @Override public ScreenRectangle scissorArea() { return null; }
    @Override public ScreenRectangle bounds() { return new ScreenRectangle(0, 0, width, height); }
}
