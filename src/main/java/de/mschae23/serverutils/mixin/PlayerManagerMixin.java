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

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import de.mschae23.serverutils.ServerUtilsMod;
import de.mschae23.serverutils.state.PlayerTeamStorage;
import de.mschae23.serverutils.state.PlayerTeamStorageContainer;
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
