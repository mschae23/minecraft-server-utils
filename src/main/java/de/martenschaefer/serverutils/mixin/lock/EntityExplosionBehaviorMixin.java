package de.martenschaefer.serverutils.mixin.lock;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.explosion.EntityExplosionBehavior;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import de.martenschaefer.serverutils.ModUtils;
import de.martenschaefer.serverutils.ServerUtilsMod;
import de.martenschaefer.serverutils.config.ContainerLockConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityExplosionBehavior.class)
public class EntityExplosionBehaviorMixin extends ExplosionBehavior {
    @Inject(method = "canDestroyBlock", at = @At("RETURN"), cancellable = true)
    private void onCanDestroyBlock(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float power, CallbackInfoReturnable<Boolean> cir) {
        ContainerLockConfig config = ServerUtilsMod.getConfig().lock();

        if (!config.enabled() || !cir.getReturnValueZ()) {
            return;
        }

        if (state.hasBlockEntity()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);

            if (blockEntity instanceof LockableContainerBlockEntity lockedBlockEntity) {
                if (explosion.getCausingEntity() instanceof PlayerEntity player) {
                    cir.setReturnValue(ModUtils.checkLockPermission(config, player, lockedBlockEntity.lock));
                } else {
                    cir.setReturnValue(ModUtils.canAlwaysOpen(lockedBlockEntity.lock));
                }
            }
        }
    }
}
