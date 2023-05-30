package de.martenschaefer.serverutils.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import de.martenschaefer.serverutils.ModUtils;
import de.martenschaefer.serverutils.ServerUtilsMod;
import de.martenschaefer.serverutils.config.ContainerLockConfig;
import de.martenschaefer.serverutils.region.RegionRuleEnforcer;
import de.martenschaefer.serverutils.state.PlayerTeamStorageContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @Shadow
    @SuppressWarnings("unused")
    private int tickCounter;

    @Shadow
    protected ServerWorld world;

    @Shadow
    @Final
    protected ServerPlayerEntity player;

    @Unique
    private int serverutils_ticksSinceUpdate = 0;

    @Unique
    private Formatting serverutils_lastFormatting = null;

    @Inject(method = "update", at = @At("RETURN"))
    private void onUpdate(CallbackInfo ci) {
        if (ServerUtilsMod.getConfig().chat().enabled() && this.serverutils_ticksSinceUpdate++ >= 100) {
            Formatting formatting = ModUtils.getUsernameFormatting(this.player);

            if (formatting == null) {
                formatting = Formatting.RESET;
            }

            if (!formatting.equals(serverutils_lastFormatting)) {
                this.serverutils_lastFormatting = formatting;

                this.player.server.getPlayerManager().sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, this.player));
                ((PlayerTeamStorageContainer) this.player.server.getPlayerManager()).getPlayerTeamStorage().updateFormatting(this.player, formatting);
            }

            this.serverutils_ticksSinceUpdate = 0;
        }
    }

    @Redirect(method = "processBlockBreakingAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;canPlayerModifyAt(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean redirectCanPlayerModifyAt(ServerWorld world, PlayerEntity player, BlockPos pos) {
        ContainerLockConfig config = ServerUtilsMod.getConfig().lock();
        boolean original = world.canPlayerModifyAt(player, pos);

        if (!config.enabled() || !original) {
            return original;
        }

        BlockState state = world.getBlockState(pos);

        if (state.hasBlockEntity()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);

            if (blockEntity instanceof LockableContainerBlockEntity lockedBlockEntity) {
                boolean hasPermission = ModUtils.checkLockPermission(config, player, lockedBlockEntity.lock);

                if (!hasPermission) {
                    player.sendMessage(Text.translatable("container.isLocked", lockedBlockEntity.getDisplayName()), true);
                    player.playSound(SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, 1.0F, 1.0F);
                }

                return hasPermission;
            }
        }

        return true;
    }

    @Inject(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;onUse(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;", ordinal = 0), cancellable = true)
    private void onUseBlock(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        ActionResult result = RegionRuleEnforcer.onBlockUse(player, hitResult.getBlockPos());

        if (result != ActionResult.PASS) {
            cir.setReturnValue(result);
        }
    }
}
