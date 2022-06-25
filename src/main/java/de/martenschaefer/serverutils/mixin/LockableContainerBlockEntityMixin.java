package de.martenschaefer.serverutils.mixin;

import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ContainerLock;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import de.martenschaefer.serverutils.ServerUtilsMod;
import de.martenschaefer.serverutils.config.ContainerLockConfig;
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

        if (!config.enabled() || lock.key.isEmpty()) {
            return true;
        }

        World world = player.getWorld();
        MinecraftServer server = world.getServer();

        if (world.isClient || server == null) {
            return lock.canOpen(stack);
        }

        String permission = lock.key;

        if (!config.permissionPrefix().isEmpty()) {
            permission = config.permissionPrefix() + "." + lock.key;
        }

        permission = ServerUtilsMod.MODID + "." + permission;

        return Permissions.check(player, permission);
    }

    @Unique
    private static LuckPerms getLuckPerms() {
        if (serverutils_luckPerms == null) {
            serverutils_luckPerms = LuckPermsProvider.get();
        }

        return serverutils_luckPerms;
    }
}
