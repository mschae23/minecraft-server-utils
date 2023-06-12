package de.martenschaefer.serverutils.mixin.lock;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import de.martenschaefer.serverutils.ModUtils;
import de.martenschaefer.serverutils.ServerUtilsMod;
import de.martenschaefer.serverutils.config.ContainerLockConfig;
import de.martenschaefer.serverutils.region.RegionRuleEnforcer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ExplosionBehavior.class)
public class ExplosionBehaviorMixin {
    @Inject(method = "canDestroyBlock", at = @At("RETURN"), cancellable = true)
    private void onCanDestroyBlock(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float power, CallbackInfoReturnable<Boolean> cir) {
        if (world instanceof ServerWorld serverWorld) {
            ActionResult result = RegionRuleEnforcer.onExplosionDestroy(serverWorld, pos);

            if (result == ActionResult.FAIL) {
                cir.setReturnValue(false);
                return;
            }
        }

        ContainerLockConfig config = ServerUtilsMod.getConfig().lock();

        if (!config.enabled() || !cir.getReturnValueZ()) {
            return;
        }

        if (state.hasBlockEntity()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);

            if (blockEntity instanceof LockableContainerBlockEntity lockedBlockEntity) {
                cir.setReturnValue(ModUtils.canAlwaysOpen(lockedBlockEntity.lock));
            }
        }
    }
}
