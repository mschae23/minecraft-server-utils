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

package de.mschae23.serverutils.mixin.lock;

import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.inventory.ContainerLock;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import de.mschae23.serverutils.ModUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {
    /*
    @Redirect(method = "getInventoryAt(Lnet/minecraft/world/World;DDD)Lnet/minecraft/inventory/Inventory;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockEntity(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/entity/BlockEntity;"))
    private static BlockEntity onGetBlockEntity(World world, BlockPos pos) {
        BlockEntity entity = world.getBlockEntity(pos);

        if (entity instanceof LockableContainerBlockEntity locked) {
            ContainerLock lock = locked.lock;

            if (!lock.key.isEmpty() || !((LockPermissionHolder) lock).getLockPermission().isEmpty()) {
                return null;
            }
        }

        return entity;
    }
    */

    @Inject(method = "canExtract", at = @At("RETURN"), cancellable = true)
    private static void onCanExtract(Inventory hopperInventory, Inventory fromInventory, ItemStack stack, int slot, Direction facing, CallbackInfoReturnable<Boolean> cir) {
        boolean isLocked;

        if (fromInventory instanceof DoubleInventory doubleInventory) {
            isLocked = serverutils_isLocked(doubleInventory.first) || serverutils_isLocked(doubleInventory.second);
        } else {
            isLocked = serverutils_isLocked(fromInventory);
        }

        if (isLocked) {
            cir.setReturnValue(false);
        }
    }

    @Unique
    private static boolean serverutils_isLocked(Inventory inventory) {
        if (inventory instanceof LockableContainerBlockEntity locked) {
            ContainerLock lock = locked.lock;
            return !ModUtils.canAlwaysOpen(lock);
        }

        return false;
    }
}
