package de.martenschaefer.serverutils.mixin.chat;

import java.util.Collection;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.command.MessageCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import de.martenschaefer.serverutils.ModUtils;
import de.martenschaefer.serverutils.ServerUtilsMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MessageCommand.class)
public class MessageCommandMixin {
    @Redirect(method = "method_45153", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/MessageCommand;execute(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/Collection;Lnet/minecraft/network/message/SignedMessage;)V"))
    private static void redirectExecute(ServerCommandSource source, Collection<ServerPlayerEntity> targets, SignedMessage message) {
        if (ServerUtilsMod.getConfig().chat().enabled()) {
            ModUtils.sendPrivateMessageFromRedirect(source, targets, message);
        } else {
            execute(source, targets, message);
        }
    }

    @Shadow
    private static void execute(ServerCommandSource source, Collection<ServerPlayerEntity> targets, SignedMessage message) {
        throw new IllegalStateException();
    }
}
