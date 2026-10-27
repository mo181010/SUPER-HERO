package de.heldenmod;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class HeldenModClient implements ClientModInitializer {
    private static final KeyMapping[] KEYS = new KeyMapping[3];

    @Override
    public void onInitializeClient() {
        KeyMapping.Category cat = KeyMapping.Category.register(HeldenMod.id("main"));
        int[] defaults = {GLFW.GLFW_KEY_R, GLFW.GLFW_KEY_G, GLFW.GLFW_KEY_V};
        for (int i = 0; i < 3; i++) {
            KEYS[i] = KeyMappingHelper.registerKeyMapping(
                    new KeyMapping("key.heldenmod.ability" + (i + 1), InputConstants.Type.KEYSYM, defaults[i], cat));
        }
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            for (int i = 0; i < 3; i++)
                while (KEYS[i].consumeClick())
                    if (mc.player != null) ClientPlayNetworking.send(new AbilityPayload(i));
        });
    }
}
