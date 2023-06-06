package de.martenschaefer.serverutils.mixin.chat;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TeamMsgCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import de.martenschaefer.serverutils.ModUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TeamMsgCommand.class)
public class TeamMsgCommandMixin {
    @Shadow
    @Final
    private static Style STYLE;

    // TODO chat.enabled config option is not respected
    /**
     * @author mschae23
     * @reason There might be a better way than overwriting here, but there needs to be a new local variable
     *         and the loop is changed.
     */
    @Overwrite
    private static void execute(ServerCommandSource source, Entity senderEntity, Team team, List<ServerPlayerEntity> recipients, SignedMessage message) {
        MutableText formattedTeamName = team.getDisplayName().copy().fillStyle(Style.EMPTY.withInsertion(team.getName()).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(team.getName()))));
        Formatting teamColor = team.getColor();

        if (teamColor != Formatting.RESET) {
            formattedTeamName.formatted(teamColor);
        }

        formattedTeamName.fillStyle(STYLE);
        MessageType.Parameters incomingParams = MessageType.params(MessageType.TEAM_MSG_COMMAND_INCOMING, source).withTargetName(formattedTeamName);
        MessageType.Parameters outgoingParams = MessageType.params(MessageType.TEAM_MSG_COMMAND_OUTGOING, source).withTargetName(formattedTeamName);

        Text decoratedIncomingMessage = ModUtils.decorateText(message.getContent(), source, incomingParams);
        Text decoratedOutgoingMessage = ModUtils.decorateText(message.getContent(), source, outgoingParams);

        for(ServerPlayerEntity target : recipients) {
            target.sendMessage(target == senderEntity ? decoratedOutgoingMessage : decoratedIncomingMessage, false);
        }
    }
}
