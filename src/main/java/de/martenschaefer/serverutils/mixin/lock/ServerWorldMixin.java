package de.martenschaefer.serverutils.mixin.lock;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import de.martenschaefer.serverutils.ModUtils;
import de.martenschaefer.serverutils.ServerUtilsMod;
import de.martenschaefer.serverutils.config.ContainerLockConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @Inject(method = "canPlayerModifyAt", at = @At("RETURN"), cancellable = true)
    private void onCanPlayerModifyAt(PlayerEntity player, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        ContainerLockConfig config = ServerUtilsMod.getConfig().lock();

        if (!config.enabled() || !cir.getReturnValueZ()) {
            return;
        }

        @SuppressWarnings("ConstantConditions")
        ServerWorld world = (ServerWorld) (Object) this;

        BlockState state = world.getBlockState(pos);

        if (state.hasBlockEntity()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);

            if (blockEntity instanceof LockableContainerBlockEntity lockedBlockEntity) {
                boolean hasPermission = ModUtils.checkLockPermission(config, player, lockedBlockEntity.lock);

                if (!hasPermission) {
                    player.sendMessage(Text.translatable("container.isLocked", lockedBlockEntity.getDisplayName()), true);
                    player.playSound(SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, 1.0F, 1.0F);
                }

                cir.setReturnValue(hasPermission);
            }
        }
    }
}
