package dev.bunar.client.ui;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static dev.bunar.client.ui.GuiSettings.Option.*;
import static org.junit.jupiter.api.Assertions.*;

class GuiSettingsTest {
    @TempDir Path directory;

    @Test void preferencesSurviveRestartAndRestoreDefaults() {
        Path file = directory.resolve("config").resolve("marce-client.properties");
        var store = new GuiSettings.Store(file);
        GuiSettings before = store.snapshot();

        store.set(HUD, 1);
        store.set(BLUR, 0);
        store.set(DIM, 67);
        store.set(ACCENT, 2);
        store.set(SCALE, 6);
        store.save();

        var reloaded = new GuiSettings.Store(file);
        assertFalse(before.enabled(HUD));
        assertTrue(reloaded.snapshot().enabled(HUD));
        assertFalse(reloaded.snapshot().enabled(BLUR));
        assertEquals(67, reloaded.snapshot().get(DIM));
        assertEquals("Ice", reloaded.snapshot().palette());
        assertEquals(110, reloaded.snapshot().scalePercent());

        reloaded.reset();
        var defaults = new GuiSettings.Store(file).snapshot();
        for (var option : GuiSettings.Option.values()) assertEquals(GuiSettings.DEFAULT.get(option), defaults.get(option));
    }

    @Test void invalidValuesDoNotBreakTheMenu() throws Exception {
        Path file = directory.resolve("marce-client.properties");
        Files.writeString(file, "DIM=135\nACCENT=-1\nSCALE=broken\n");
        var preferences = new GuiSettings.Store(file).snapshot();
        assertEquals(100, preferences.get(DIM));
        assertEquals("Lilac", preferences.palette());
        assertEquals(100, preferences.scalePercent());
    }

    @Test void clickTargetsMatchTheScaledControls() {
        PanelLayout layout = PanelLayout.forWindow(400, 300, 100);
        var row = PanelLayout.rowBounds(1, 1);
        double screenX = layout.x() + PanelLayout.trackX(row) * layout.scale();
        double screenY = layout.y() + (row.y() + 25) * layout.scale();
        assertEquals(SCALE, layout.controlAt(Category.VISUAL, screenX, screenY).option());
        assertEquals(COORDINATES, layout.controlAt(Category.HUD, screenX, screenY).option());
        assertNull(layout.controlAt(Category.SETTINGS, screenX, screenY));
    }
}
