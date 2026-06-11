/*
 * Copyright (C) 2026  mschae23
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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.mschae23.serverutils.ServerUtilsMod;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.command.permission.PermissionCheck;
import net.minecraft.command.permission.PermissionPredicate;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow
    private ServerPlayerEntity player;

    @WrapOperation(method = "onChangeGameMode", at = @At(value = "INVOKE", target = "Lnet/minecraft/command/permission/PermissionCheck;allows(Lnet/minecraft/command/permission/PermissionPredicate;)Z"))
    private boolean allowGameMode(PermissionCheck instance, PermissionPredicate predicate, Operation<Boolean> operation) {
        TriState value = Permissions.getPermissionValue(this.player, ServerUtilsMod.getConfig().misc().enableGamemodeSwitcher().permission());
        return value.orElseGet(() -> operation.call(instance, predicate));
    }
}
