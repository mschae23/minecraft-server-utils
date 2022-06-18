package de.martenschaefer.serverutils.mixin;

import net.minecraft.network.message.MessageType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.registry.RegistryKey;
import de.martenschaefer.serverutils.ServerUtilsMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @ModifyArg(method = "handleDecoratedMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/server/filter/FilteredMessage;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/util/registry/RegistryKey;)V"),
        index = 2)
    private RegistryKey<MessageType> changeMessageType(RegistryKey<?> original) {
        return ServerUtilsMod.UNDECORATED_CHAT;
    }
}
