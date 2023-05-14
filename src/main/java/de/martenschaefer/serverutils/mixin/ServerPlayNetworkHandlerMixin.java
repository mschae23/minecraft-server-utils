package de.martenschaefer.serverutils.mixin;

import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import de.martenschaefer.serverutils.ServerUtilsMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Redirect(method = "handleDecoratedMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/network/message/SignedMessage;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/MessageType$Parameters;)V"))
    private void changeMessageType(PlayerManager manager, SignedMessage message, ServerPlayerEntity sender, MessageType.Parameters parameters) {
        if (!ServerUtilsMod.getConfig().chat().enabled()) {
            manager.broadcast(message, sender, parameters);
            return;
        }

        System.out.println("Redirected message: " + message.getContent());

        manager.broadcast(message.getContent(), false);
    }
}
