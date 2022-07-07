package de.martenschaefer.serverutils.mixin.lock;

import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.inventory.ContainerLock;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import de.martenschaefer.serverutils.ModUtils;
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
    private static void onCanExtract(Inventory inv, ItemStack stack, int slot, Direction facing, CallbackInfoReturnable<Boolean> cir) {
        boolean isLocked;

        if (inv instanceof DoubleInventory doubleInventory) {
            isLocked = serverutils_isLocked(doubleInventory.first) || serverutils_isLocked(doubleInventory.second);
        } else {
            isLocked = serverutils_isLocked(inv);
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
