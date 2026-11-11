package dev.bunar.client.ui;

public enum Category {
    HUD("HUD", "Arrange your overlay"),
    VISUAL("Visual", "Make it yours"),
    PLAYER("Player", "Your everyday tools"),
    SETTINGS("Settings", "Client preferences");
    public final String label;
    public final String description;
    Category(String label, String description) { this.label = label; this.description = description; }
}
