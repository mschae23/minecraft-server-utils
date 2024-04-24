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

import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ContainerLock;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import de.mschae23.serverutils.ModUtils;
import de.mschae23.serverutils.ServerUtilsMod;
import de.mschae23.serverutils.config.ContainerLockConfig;
import de.mschae23.serverutils.holder.LockPermissionHolder;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LockableContainerBlockEntity.class)
public class LockableContainerBlockEntityMixin {
    @Unique
    private static LuckPerms serverutils_luckPerms = null;

    @Redirect(method = "checkUnlocked(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/inventory/ContainerLock;Lnet/minecraft/text/Text;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/ContainerLock;canOpen(Lnet/minecraft/item/ItemStack;)Z"))
    private static boolean redirectCanOpen(ContainerLock lock, ItemStack stack, PlayerEntity player, ContainerLock lock2, Text containerName) {
        ContainerLockConfig config = ServerUtilsMod.getConfig().lock();

        LockPermissionHolder lockPermission = (LockPermissionHolder) lock;

        World world = player.getWorld();
        MinecraftServer server = world.getServer();

        if (!config.enabled() || lockPermission.getLockPermission().isEmpty() || world.isClient || server == null) {
            return lock.canOpen(stack);
        }

        String permission = ModUtils.getLockPermission(config, lockPermission);
        return Permissions.check(player, permission) && lock.canOpen(stack);
    }

    @Unique
    private static LuckPerms getLuckPerms() {
        if (serverutils_luckPerms == null) {
            serverutils_luckPerms = LuckPermsProvider.get();
        }

        return serverutils_luckPerms;
    }
}
