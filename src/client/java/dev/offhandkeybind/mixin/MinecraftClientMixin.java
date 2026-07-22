package dev.offhandkeybind.mixin;

import dev.offhandkeybind.OffhandKeybindClient;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes vanilla use main-hand-only and routes the dedicated binding through
 * the very same vanilla use routine with the off hand selected instead.
 */
@Mixin(Minecraft.class)
abstract class MinecraftClientMixin {
    @Shadow
    @Final
    public Options options;

    @Shadow
    private int rightClickDelay;

    @Unique
    private int offhandkeybind$queuedOffhandPresses;

    /**
     * The normal Use key reaches this method without an off-hand context, so
     * its loop contains only MAIN_HAND. Calls made by the dedicated key have
     * the context set and receive only OFF_HAND.
     */
    @Redirect(
            method = "startUseItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/InteractionHand;values()[Lnet/minecraft/world/InteractionHand;"
            )
    )
    private InteractionHand[] offhandkeybind$chooseOnlyOneHand() {
        return OffhandKeybindClient.isOffhandUseActive()
                ? new InteractionHand[]{InteractionHand.OFF_HAND}
                : new InteractionHand[]{InteractionHand.MAIN_HAND};
    }

    /**
     * Mouse 3 is Pick Block by default. When it is also the dedicated
     * off-hand key, consume that shared press here so using the default key
     * does not additionally trigger Pick Block in creative mode.
     */
    @Redirect(
            method = "handleKeybinds",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z"
            )
    )
    private boolean offhandkeybind$skipConflictingPickBlock(KeyMapping keyBinding) {
        if (keyBinding == this.options.keyPickItem && offhandkeybind$sharesBindingWith(keyBinding)) {
            while (OffhandKeybindClient.OFFHAND_USE_KEY.consumeClick()) {
                this.offhandkeybind$queuedOffhandPresses++;
            }

            return false;
        }

        return keyBinding.consumeClick();
    }

    /**
     * Runs after vanilla has consumed its own input. This mirrors the normal
     * held-use check so bows, food, shields, and other held interactions keep
     * their expected repeat/cooldown behavior.
     */
    @Inject(method = "handleKeybinds", at = @At("TAIL"))
    private void offhandkeybind$handleDedicatedOffhandKey(CallbackInfo ci) {
        Minecraft client = (Minecraft) (Object) this;

        if (client.player == null) {
            this.offhandkeybind$queuedOffhandPresses = 0;
            while (OffhandKeybindClient.OFFHAND_USE_KEY.consumeClick()) {
                // Consume presses that happened while no world was loaded.
            }
            return;
        }

        while (this.offhandkeybind$queuedOffhandPresses > 0) {
            this.offhandkeybind$queuedOffhandPresses--;
            offhandkeybind$useOffhandOnly();
        }

        while (OffhandKeybindClient.OFFHAND_USE_KEY.consumeClick()) {
            offhandkeybind$useOffhandOnly();
        }

        if (OffhandKeybindClient.OFFHAND_USE_KEY.isDown()
                && this.rightClickDelay == 0
                && !client.player.isUsingItem()) {
            offhandkeybind$useOffhandOnly();
        }
    }

    @Unique
    private void offhandkeybind$useOffhandOnly() {
        OffhandKeybindClient.useOffhandOnly(
                () -> ((MinecraftClientInvoker) (Object) this).offhandkeybind$invokeStartUseItem()
        );
    }

    @Unique
    private boolean offhandkeybind$sharesBindingWith(KeyMapping vanillaKey) {
        return !OffhandKeybindClient.OFFHAND_USE_KEY.isUnbound()
                && OffhandKeybindClient.OFFHAND_USE_KEY.same(vanillaKey);
    }
}
