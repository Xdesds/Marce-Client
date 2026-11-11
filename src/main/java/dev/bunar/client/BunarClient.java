package dev.bunar.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.bunar.client.render.BunarGuiRenderer;
import dev.bunar.client.render.RenderPreparation;
import dev.bunar.client.ui.ClickGuiScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class BunarClient implements ClientModInitializer {
    private static final RenderPreparation PREPARATION = new RenderPreparation();
    public static RenderPreparation preparation() { return PREPARATION; }
    @Override public void onInitializeClient() {
        var category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("bunar", "client"));
        var open = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.bunar.open_gui", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        SpecialGuiElementRegistry.register(context -> new BunarGuiRenderer(context.vertexConsumers()));
        ClientTickEvents.END_CLIENT_TICK.register(client -> { while (open.consumeClick()) client.setScreen(client.screen instanceof ClickGuiScreen ? null : new ClickGuiScreen()); });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> PREPARATION.close());
    }
}
