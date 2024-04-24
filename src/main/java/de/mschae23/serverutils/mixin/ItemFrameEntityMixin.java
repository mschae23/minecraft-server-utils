/*
 * Copyright (C) 2024  mschae23
 *
 * This file is part of Server utils.
 *
 * Server utils is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.mschae23.serverutils.mixin;

import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.event.GameEvent;
import de.mschae23.serverutils.ServerUtilsMod;
import de.mschae23.serverutils.config.ItemFrameConfig;
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
