package de.martenschaefer.serverutils.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import de.martenschaefer.serverutils.ModUtils;
import de.martenschaefer.serverutils.ServerUtilsMod;
import de.martenschaefer.serverutils.config.ContainerLockConfig;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
    private static LuckPerms serverutils_luckPerms = null;

    @Unique
    private int serverutils_ticksSinceUpdate = 0;

    @Unique
    private Formatting serverutils_lastFormatting = null;

    @Unique
    @Nullable
    private Team serverutils_clientTeam = null;

    @Inject(method = "update", at = @At("RETURN"))
    private void onUpdate(CallbackInfo ci) {
        if (ServerUtilsMod.getConfig().chat().enabled() && this.serverutils_ticksSinceUpdate++ >= 100) {
            User user = getLuckPerms().getPlayerAdapter(ServerPlayerEntity.class).getUser(this.player);
            String colorName = user.getCachedData().getMetaData().getMetaValue("username-color");
            Formatting formatting = Formatting.byName(colorName);

            if (formatting == null) {
                formatting = Formatting.RESET;
            }

            if (colorName != null && !formatting.equals(serverutils_lastFormatting)) {
                if (this.serverutils_clientTeam == null) {
                    this.serverutils_clientTeam = new Team(this.world.getScoreboard(), ServerUtilsMod.MODID + "_player_" + this.player.getEntityName());
                    this.serverutils_clientTeam.getPlayerList().add(this.player.getEntityName());
                }

                this.serverutils_lastFormatting = formatting;
                this.serverutils_clientTeam.setColor(formatting);
                this.player.server.getPlayerManager().sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, this.player));
                // This is suboptimal; every time someone joins or changes color, everyone will get a warning in their
                // client logs. But it works for now.
                this.player.server.getPlayerManager().sendToAll(TeamS2CPacket.updateTeam(this.serverutils_clientTeam, true));
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

    @Unique
    private static LuckPerms getLuckPerms() {
        if (serverutils_luckPerms == null) {
            serverutils_luckPerms = LuckPermsProvider.get();
        }

        return serverutils_luckPerms;
    }
}
