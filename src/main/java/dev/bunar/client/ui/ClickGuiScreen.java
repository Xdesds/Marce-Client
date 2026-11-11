package dev.bunar.client.ui;

import dev.bunar.client.render.BunarRenderState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class ClickGuiScreen extends Screen {
    private int selected;
    public ClickGuiScreen() { super(Component.literal("Bunar Client")); }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        PanelLayout p = PanelLayout.forWindow(width, height);
        graphics.guiRenderState.submitPicturesInPictureState(new BunarRenderState(width, height, p, selected, p.categoryAt(mouseX, mouseY)));
    }
    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int category = PanelLayout.forWindow(width,height).categoryAt(event.x(),event.y());
        if (event.button()==0 && category>=0) { selected=category; return true; }
        return super.mouseClicked(event,doubleClick);
    }
}
