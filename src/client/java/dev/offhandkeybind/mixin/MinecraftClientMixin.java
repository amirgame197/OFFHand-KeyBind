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

@Mixin(Minecraft.class)
abstract class MinecraftClientMixin {
    @Shadow
    @Final
    public Options options;

    @Shadow
    private int rightClickDelay;

    @Unique
    private int offhandkeybind$queuedOffhandPresses;

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

    @Inject(method = "handleKeybinds", at = @At("TAIL"))
    private void offhandkeybind$handleDedicatedOffhandKey(CallbackInfo ci) {
        Minecraft client = (Minecraft) (Object) this;

        if (client.player == null) {
            this.offhandkeybind$queuedOffhandPresses = 0;
            while (OffhandKeybindClient.OFFHAND_USE_KEY.consumeClick()) {}
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
