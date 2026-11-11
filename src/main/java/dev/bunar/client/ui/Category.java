package dev.bunar.client.ui;

import java.util.List;
import static dev.bunar.client.ui.GuiSettings.Option.*;

public enum Category {
    VISUAL("Visual", "Tune the client interface",
            new Group("Backdrop", BLUR, DIM), new Group("Appearance", ACCENT, SCALE)),
    HUD("HUD", "Choose what you see in-game",
            new Group("Overlay", GuiSettings.Option.HUD, COMPACT), new Group("Readouts", FPS, COORDINATES)),
    SETTINGS("Settings", "Manage client preferences",
            new Group("Preferences", HELP), new Group("Keyboard"));

    final String label;
    final String description;
    final List<Group> groups;

    Category(String label, String description, Group left, Group right) {
        this.label = label;
        this.description = description;
        this.groups = List.of(left, right);
    }

    record Group(String title, List<GuiSettings.Option> options) {
        Group(String title, GuiSettings.Option... options) { this(title, List.of(options)); }
    }
}
