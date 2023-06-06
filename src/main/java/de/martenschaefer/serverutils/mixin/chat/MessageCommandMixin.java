package de.martenschaefer.serverutils.mixin.chat;

import java.util.Collection;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.command.MessageCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import de.martenschaefer.serverutils.ModUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(MessageCommand.class)
public class MessageCommandMixin {
    // TODO chat.enabled config option is not respected
    /**
     * @author mschae23
     * @reason There might be a better way than overwriting here, but there needs to be a new local variable
     *         and the loop is changed.
     */
    @Overwrite
    private static void execute(ServerCommandSource source, Collection<ServerPlayerEntity> targets, SignedMessage message) {
        MessageType.Parameters incomingParams = MessageType.params(MessageType.MSG_COMMAND_INCOMING, source);
        Text decoratedIncomingMessage = ModUtils.decorateText(message.getContent(), source, incomingParams);

        for(ServerPlayerEntity target : targets) {
            MessageType.Parameters outgoingParams = MessageType.params(MessageType.MSG_COMMAND_OUTGOING, source)
                .withTargetName(target.getDisplayName());
            Text decoratedOutgoingMessage = ModUtils.decorateText(message.getContent(), source, outgoingParams);

            source.sendMessage(decoratedOutgoingMessage);
            target.sendMessage(decoratedIncomingMessage, false);
        }
    }
}
