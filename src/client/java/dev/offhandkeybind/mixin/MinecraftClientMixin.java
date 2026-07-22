package dev.offhandkeybind.mixin;

import dev.offhandkeybind.OffhandKeybindClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Hand;
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
@Mixin(MinecraftClient.class)
abstract class MinecraftClientMixin {
    @Shadow
    @Final
    public GameOptions options;

    @Shadow
    private int itemUseCooldown;

    @Unique
    private int offhandkeybind$queuedOffhandPresses;

    /**
     * The normal Use key reaches this method without an off-hand context, so
     * its loop contains only MAIN_HAND. Calls made by the dedicated key have
     * the context set and receive only OFF_HAND.
     */
    @Redirect(
            method = "doItemUse",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Hand;values()[Lnet/minecraft/util/Hand;"
            )
    )
    private Hand[] offhandkeybind$chooseOnlyOneHand() {
        return OffhandKeybindClient.isOffhandUseActive()
                ? new Hand[]{Hand.OFF_HAND}
                : new Hand[]{Hand.MAIN_HAND};
    }

    /**
     * Mouse 3 is Pick Block by default. When it is also the dedicated
     * off-hand key, consume that shared press here so using the default key
     * does not additionally trigger Pick Block in creative mode.
     */
    @Redirect(
            method = "handleInputEvents",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/option/KeyBinding;wasPressed()Z"
            )
    )
    private boolean offhandkeybind$skipConflictingPickBlock(KeyBinding keyBinding) {
        if (keyBinding == this.options.pickItemKey && offhandkeybind$sharesBindingWith(keyBinding)) {
            while (OffhandKeybindClient.OFFHAND_USE_KEY.wasPressed()) {
                this.offhandkeybind$queuedOffhandPresses++;
            }

            return false;
        }

        return keyBinding.wasPressed();
    }

    /**
     * Runs after vanilla has consumed its own input. This mirrors the normal
     * held-use check so bows, food, shields, and other held interactions keep
     * their expected repeat/cooldown behavior.
     */
    @Inject(method = "handleInputEvents", at = @At("TAIL"))
    private void offhandkeybind$handleDedicatedOffhandKey(CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;

        if (client.player == null) {
            this.offhandkeybind$queuedOffhandPresses = 0;
            while (OffhandKeybindClient.OFFHAND_USE_KEY.wasPressed()) {
                // Consume presses that happened while no world was loaded.
            }
            return;
        }

        while (this.offhandkeybind$queuedOffhandPresses > 0) {
            this.offhandkeybind$queuedOffhandPresses--;
            offhandkeybind$useOffhandOnly();
        }

        while (OffhandKeybindClient.OFFHAND_USE_KEY.wasPressed()) {
            offhandkeybind$useOffhandOnly();
        }

        if (OffhandKeybindClient.OFFHAND_USE_KEY.isPressed()
                && this.itemUseCooldown == 0
                && !client.player.isUsingItem()) {
            offhandkeybind$useOffhandOnly();
        }
    }

    @Unique
    private void offhandkeybind$useOffhandOnly() {
        OffhandKeybindClient.useOffhandOnly(
                () -> ((MinecraftClientInvoker) (Object) this).offhandkeybind$invokeDoItemUse()
        );
    }

    @Unique
    private boolean offhandkeybind$sharesBindingWith(KeyBinding vanillaKey) {
        return !OffhandKeybindClient.OFFHAND_USE_KEY.isUnbound()
                && OffhandKeybindClient.OFFHAND_USE_KEY.getBoundKeyTranslationKey()
                .equals(vanillaKey.getBoundKeyTranslationKey());
    }
}
