package de.martenschaefer.serverutils.mixin;

import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.event.GameEvent;
import de.martenschaefer.serverutils.ServerUtilsMod;
import de.martenschaefer.serverutils.config.ItemFrameConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrameEntity.class)
public class ItemFrameEntityMixin {
    @Inject(method = "interact", at = @At(value = "INVOKE", target = "net/minecraft/entity/decoration/ItemFrameEntity.playSound(Lnet/minecraft/sound/SoundEvent;FF)V", ordinal = 0), cancellable = true)
    private void onInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemFrameConfig config = ServerUtilsMod.getConfig().misc().itemFrame();

        if (!config.enabled()) {
            return;
        }

        // Can't capture the locals, because they are gone for some reason
        ItemStack playerHandStack = player.getStackInHand(hand);
        boolean playerHoldsItem = !playerHandStack.isEmpty();

        if (playerHoldsItem && playerHandStack.isOf(config.invisibilityItem())) {
            ItemFrameEntity self = (ItemFrameEntity) (Object) this;

            self.setInvisible(!self.isInvisible());
            self.emitGameEvent(GameEvent.BLOCK_CHANGE, player);

            if (!player.getAbilities().creativeMode) {
                playerHandStack.decrement(1);
            }

            cir.setReturnValue(ActionResult.CONSUME);
        }
    }
}
