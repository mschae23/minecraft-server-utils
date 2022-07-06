package de.martenschaefer.serverutils.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import de.martenschaefer.serverutils.ServerUtilsMod;
import me.lucko.fabric.api.permissions.v0.Permissions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @SuppressWarnings({ "MixinAnnotationTarget", "InvalidMemberReference", "UnresolvedMixinReference", "InvalidInjectorMethodSignature" })
    @Redirect(method = "sendCommandTree(Lnet/minecraft/server/network/ServerPlayerEntity;I)V", at = @At(value = "NEW", target = "(Lnet/minecraft/entity/Entity;B)Lnet/minecraft/network/packet/s2c/play/EntityStatusS2CPacket;"))
    private EntityStatusS2CPacket onCreateSetOpLevelPacket(Entity entity, byte status, ServerPlayerEntity player, int permissionLevel) {
        if (permissionLevel >= 2 || !Permissions.check(player, ServerUtilsMod.getConfig().misc().enableGamemodeSwitcher().permission(), false)) {
            return new EntityStatusS2CPacket(entity, status);
        }

        return new EntityStatusS2CPacket(entity, EntityStatuses.SET_OP_LEVEL_2);
    }
}
