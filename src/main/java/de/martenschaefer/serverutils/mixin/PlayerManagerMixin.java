package de.martenschaefer.serverutils.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import de.martenschaefer.serverutils.ServerUtilsMod;
import de.martenschaefer.serverutils.state.PlayerTeamStorage;
import de.martenschaefer.serverutils.state.PlayerTeamStorageContainer;
import me.lucko.fabric.api.permissions.v0.Permissions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin implements PlayerTeamStorageContainer {
    @Unique
    private final PlayerTeamStorage playerTeamStorage = new PlayerTeamStorage();

    @Redirect(method = "sendCommandTree(Lnet/minecraft/server/network/ServerPlayerEntity;I)V", at = @At(value = "NEW", target = "(Lnet/minecraft/entity/Entity;B)Lnet/minecraft/network/packet/s2c/play/EntityStatusS2CPacket;"))
    private EntityStatusS2CPacket onCreateSetOpLevelPacket(Entity entity, byte status, ServerPlayerEntity player, int permissionLevel) {
        if (permissionLevel >= 2 || !Permissions.check(player, ServerUtilsMod.getConfig().misc().enableGamemodeSwitcher().permission(), false)) {
            return new EntityStatusS2CPacket(entity, status);
        }

        return new EntityStatusS2CPacket(entity, EntityStatuses.SET_OP_LEVEL_2);
    }

    @Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendScoreboard(Lnet/minecraft/scoreboard/ServerScoreboard;Lnet/minecraft/server/network/ServerPlayerEntity;)V", shift = At.Shift.AFTER))
    private void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        PlayerTeamStorage storage = this.getPlayerTeamStorage();
        storage.onPlayerConnect(player);
    }

    @Inject(method = "remove", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;detach()V"))
    private void onPlayerDisconnect(ServerPlayerEntity player, CallbackInfo ci) {
        PlayerTeamStorage storage = this.getPlayerTeamStorage();
        storage.onPlayerDisconnect(player);
    }

    @Override
    public PlayerTeamStorage getPlayerTeamStorage() {
        return this.playerTeamStorage;
    }
}
