package dev.offhandkeybind;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class OffhandKeybindClient implements ClientModInitializer {
    public static final String MOD_ID = "offhandkeybind";

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(MOD_ID, "gameplay")
    );

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
    public void onInitializeClient() {}

    public static boolean isOffhandUseActive() {
        return OFFHAND_USE_ACTIVE.get();
    }

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
