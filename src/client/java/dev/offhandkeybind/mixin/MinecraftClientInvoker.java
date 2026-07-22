package dev.offhandkeybind.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Exposes Minecraft's private, vanilla item-use implementation to the keybind mixin. */
@Mixin(Minecraft.class)
public interface MinecraftClientInvoker {
    @Invoker("startUseItem")
    void offhandkeybind$invokeStartUseItem();
}
