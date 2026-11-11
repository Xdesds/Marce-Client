package dev.bunar.client.ui;

import com.mojang.logging.LogUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Properties;

/** Immutable preferences shared by the HUD and the deferred renderer. */
public final class GuiSettings {
    public enum Option {
        HUD("Show HUD", "Display the overlay in-game.", 1, 0),
        COMPACT("Compact layout", "Show readouts on a single line.", 1, 0),
        FPS("Frame rate", "Show frames per second.", 1, 1),
        COORDINATES("Coordinates", "Show your current block position.", 1, 1),
        BLUR("Background blur", "Soften the world behind this menu.", 1, 1),
        DIM("Background dim", "Adjust how much the world is darkened.", 100, 45),
        ACCENT("Accent color", "Click to cycle through the palettes.", 3, 0),
        SCALE("Interface scale", "Set the maximum size of this panel.", 6, 4),
        HELP("Setting descriptions", "Show help when you hover a setting.", 1, 1);

        public final String label;
        public final String description;
        public final int max;
        private final int initial;

        Option(String label, String description, int max, int initial) {
            this.label = label;
            this.description = description;
            this.max = max;
            this.initial = initial;
        }

        boolean slider() { return this == DIM || this == SCALE; }
    }

    private static final int[] ACCENTS = {0xffc59af8, 0xffff9278, 0xff8ed2f2, 0xff8edbb6};
    private static final String[] PALETTES = {"Lilac", "Mars", "Ice", "Mint"};
    public static final GuiSettings DEFAULT = new GuiSettings(Arrays.stream(Option.values())
            .mapToInt(option -> option.initial).toArray());
    private final int[] values;

    private GuiSettings(int[] values) { this.values = values; }

    public int get(Option option) { return values[option.ordinal()]; }
    public boolean enabled(Option option) { return get(option) != 0; }
    public int accent() { return ACCENTS[get(Option.ACCENT)]; }
    String palette() { return PALETTES[get(Option.ACCENT)]; }
    int scalePercent() { return 80 + 5 * get(Option.SCALE); }

    GuiSettings with(Option option, int value) {
        int next = Math.clamp(value, 0, option.max);
        if (next == get(option)) return this;
        int[] copy = values.clone();
        copy[option.ordinal()] = next;
        return new GuiSettings(copy);
    }

    public static final class Store {
        private final Path file;
        private GuiSettings settings = DEFAULT;
        private boolean dirty;

        public Store(Path file) {
            this.file = file;
            if (!Files.isRegularFile(file)) return;
            var properties = new Properties();
            try (var input = Files.newInputStream(file)) {
                properties.load(input);
                for (Option option : Option.values()) {
                    String value = properties.getProperty(option.name());
                    if (value == null) continue;
                    try { settings = settings.with(option, Integer.parseInt(value)); }
                    catch (NumberFormatException ignored) { /* Keep this option's default. */ }
                }
            } catch (IOException | IllegalArgumentException error) {
                LogUtils.getLogger().warn("Could not load Marce settings", error);
            }
        }

        public GuiSettings snapshot() { return settings; }

        void set(Option option, int value) {
            GuiSettings next = settings.with(option, value);
            dirty |= next != settings;
            settings = next;
        }

        void reset() {
            settings = DEFAULT;
            dirty = true;
            save();
        }

        public void save() {
            if (!dirty) return;
            var properties = new Properties();
            for (Option option : Option.values()) properties.setProperty(option.name(), Integer.toString(settings.get(option)));
            Path temp = null;
            try {
                Files.createDirectories(file.getParent());
                temp = Files.createTempFile(file.getParent(), "marce-", ".tmp");
                try (var output = Files.newOutputStream(temp)) { properties.store(output, "Marce Client"); }
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                dirty = false;
            } catch (IOException error) {
                LogUtils.getLogger().warn("Could not save Marce settings", error);
            } finally {
                if (temp != null) {
                    try { Files.deleteIfExists(temp); }
                    catch (IOException error) { LogUtils.getLogger().debug("Could not remove temporary settings", error); }
                }
            }
        }
    }
}
