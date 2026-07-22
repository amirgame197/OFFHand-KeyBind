package dev.offhandkeybind.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Exposes Minecraft's private, vanilla item-use implementation to the keybind mixin. */
@Mixin(MinecraftClient.class)
public interface MinecraftClientInvoker {
    @Invoker("doItemUse")
    void offhandkeybind$invokeDoItemUse();
}
