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

import net.minecraft.inventory.ContainerLock;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import de.mschae23.serverutils.ServerUtilsMod;
import de.mschae23.serverutils.config.ContainerLockConfig;
import de.mschae23.serverutils.holder.LockPermissionHolder;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ContainerLock.class)
public class ContainerLockMixin implements LockPermissionHolder {
    @Unique
    private static LuckPerms serverutils_luckPerms = null;

    @Unique
    private String serverutils_permission = "";

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public @NotNull String getLockPermission() {
        return this.serverutils_permission;
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public void setLockPermission(String permission) {
        this.serverutils_permission = permission;
    }

    @Inject(method = "fromNbt", at = @At("RETURN"), cancellable = true)
    private static void onFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries, CallbackInfoReturnable<ContainerLock> cir) {
        ContainerLockConfig config = ServerUtilsMod.getConfig().lock();

        if (!config.enabled()) {
            return;
        }

        ContainerLock original = cir.getReturnValue();
        String permission = "";

        if (nbt.contains(config.dataKey(), NbtElement.STRING_TYPE)) {
            permission = nbt.getString(config.dataKey());
        }

        ContainerLock modified = new ContainerLock(original.predicate());
        ((ContainerLockMixin) (Object) modified).serverutils_permission = permission;
        cir.setReturnValue(modified);
    }

    @Inject(method = "writeNbt", at = @At("RETURN"))
    private void onWriteNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries, CallbackInfo ci) {
        ContainerLockConfig config = ServerUtilsMod.getConfig().lock();

        if (!config.enabled()) {
            return;
        }

        if (!this.serverutils_permission.isEmpty()) {
            nbt.putString(config.dataKey(), this.serverutils_permission);
        }
    }

    @Unique
    private static LuckPerms getLuckPerms() {
        if (serverutils_luckPerms == null) {
            serverutils_luckPerms = LuckPermsProvider.get();
        }

        return serverutils_luckPerms;
    }
}
