package dev.bunar.client.ui;

import dev.bunar.client.render.BunarRenderState;
import dev.bunar.client.ui.GuiSettings.Option;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class ClickGuiScreen extends Screen {
    private final GuiSettings.Store settings;
    private Category selected = Category.VISUAL;
    private PanelLayout.Control drag;
    private PanelLayout dragLayout;

    public ClickGuiScreen(GuiSettings.Store settings) {
        super(Component.literal("Marce Client"));
        this.settings = settings;
    }

    @Override public boolean isPauseScreen() { return false; }

    private PanelLayout layout() {
        return dragLayout != null ? dragLayout :
                PanelLayout.forWindow(width, height, settings.snapshot().scalePercent());
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.guiRenderState.submitPicturesInPictureState(new BunarRenderState(
                width, height, layout(), selected, settings.snapshot(), mouseX, mouseY));
    }

    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return super.mouseClicked(event, doubleClick);
        PanelLayout layout = layout();
        Category category = layout.categoryAt(event.x(), event.y());
        if (category != null) {
            selected = category;
            return true;
        }

        if (selected == Category.SETTINGS &&
                PanelLayout.RESET.contains(layout.localX(event.x()), layout.localY(event.y()))) {
            settings.reset();
            return true;
        }

        var control = layout.controlAt(selected, event.x(), event.y());
        if (control == null) return super.mouseClicked(event, doubleClick);
        Option option = control.option();
        if (option.slider()) {
            float x = layout.localX(event.x()), track = PanelLayout.trackX(control.bounds());
            if (x < track - 6 || x > track + PanelLayout.TRACK_WIDTH + 6) return false;
            drag = control;
            dragLayout = layout;
            updateSlider(event.x());
        } else {
            settings.set(option, (settings.snapshot().get(option) + 1) % (option.max + 1));
            settings.save();
        }
        return true;
    }

    @Override public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (drag == null) return super.mouseDragged(event, deltaX, deltaY);
        updateSlider(event.x());
        return true;
    }

    private void updateSlider(double mouseX) {
        float fraction = (dragLayout.localX(mouseX) - PanelLayout.trackX(drag.bounds())) / PanelLayout.TRACK_WIDTH;
        settings.set(drag.option(), Math.round(Math.clamp(fraction, 0f, 1f) * drag.option().max));
    }

    @Override public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT || drag == null) return super.mouseReleased(event);
        drag = null;
        dragLayout = null;
        settings.save();
        return true;
    }

    @Override public boolean keyPressed(KeyEvent event) {
        int index = event.key() - GLFW.GLFW_KEY_1;
        var categories = Category.values();
        if (index >= 0 && index < categories.length) {
            selected = categories[index];
            return true;
        }
        return super.keyPressed(event);
    }

    @Override public void removed() {
        drag = null;
        dragLayout = null;
        settings.save();
        super.removed();
    }
}
