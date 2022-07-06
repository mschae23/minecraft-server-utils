package de.martenschaefer.serverutils.mixin.lock;

import net.minecraft.inventory.ContainerLock;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import de.martenschaefer.serverutils.ServerUtilsMod;
import de.martenschaefer.serverutils.config.ContainerLockConfig;
import de.martenschaefer.serverutils.holder.LockPermissionHolder;
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

    @Override
    public @NotNull String getLockPermission() {
        return this.serverutils_permission;
    }

    @Inject(method = "fromNbt", at = @At("RETURN"), cancellable = true)
    private static void onFromNbt(NbtCompound nbt, CallbackInfoReturnable<ContainerLock> cir) {
        ContainerLockConfig config = ServerUtilsMod.getConfig().lock();

        if (!config.enabled()) {
            return;
        }

        ContainerLock original = cir.getReturnValue();
        String permission = "";

        if (nbt.contains(config.dataKey(), NbtElement.STRING_TYPE)) {
            permission = nbt.getString(config.dataKey());
        }

        ContainerLock modified = new ContainerLock(original.key);
        ((ContainerLockMixin) (Object) modified).serverutils_permission = permission;
        cir.setReturnValue(modified);
    }

    @Inject(method = "writeNbt", at = @At("RETURN"))
    private void onWriteNbt(NbtCompound nbt, CallbackInfo ci) {
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
