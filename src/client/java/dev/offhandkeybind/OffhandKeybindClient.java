package dev.offhandkeybind;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * Registers the user-configurable off-hand-only item-use keybind.
 */
public final class OffhandKeybindClient implements ClientModInitializer {
    public static final String MOD_ID = "offhandkeybind";

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(MOD_ID, "gameplay")
    );

    /**
     * Middle mouse button is GLFW mouse button 2, commonly labelled Mouse 3 in
     * Minecraft's controls menu.
     */
    public static final KeyMapping OFFHAND_USE_KEY = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.offhandkeybind.offhand_use",
                    InputConstants.Type.MOUSE,
                    GLFW.GLFW_MOUSE_BUTTON_MIDDLE,
                    CATEGORY
            )
    );

    private static final ThreadLocal<Boolean> OFFHAND_USE_ACTIVE = ThreadLocal.withInitial(() -> false);

    @Override
    public void onInitializeClient() {
        // The static keybinding registration above is all this client entrypoint needs.
    }

    /**
     * Returns whether the current call into Minecraft's use routine is a
     * dedicated off-hand use rather than a normal vanilla use.
     */
    public static boolean isOffhandUseActive() {
        return OFFHAND_USE_ACTIVE.get();
    }

    /**
     * Marks exactly one call to Minecraft's normal item-use routine as an
     * off-hand-only invocation. The ThreadLocal keeps this safe if another mod
     * nests a call on the client thread.
     */
    public static void useOffhandOnly(Runnable action) {
        boolean previousState = OFFHAND_USE_ACTIVE.get();
        OFFHAND_USE_ACTIVE.set(true);

        try {
            action.run();
        } finally {
            if (previousState) {
                OFFHAND_USE_ACTIVE.set(true);
            } else {
                OFFHAND_USE_ACTIVE.remove();
            }
        }
    }
}
