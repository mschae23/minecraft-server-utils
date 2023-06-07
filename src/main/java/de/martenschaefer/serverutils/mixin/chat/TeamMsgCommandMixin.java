package de.martenschaefer.serverutils.mixin.chat;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TeamMsgCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import de.martenschaefer.serverutils.ModUtils;
import de.martenschaefer.serverutils.ServerUtilsMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TeamMsgCommand.class)
public class TeamMsgCommandMixin {
    @Redirect(method = "method_45155", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/TeamMsgCommand;execute(Lnet/minecraft/server/command/ServerCommandSource;Lnet/minecraft/entity/Entity;Lnet/minecraft/scoreboard/Team;Ljava/util/List;Lnet/minecraft/network/message/SignedMessage;)V"))
    private static void redirectExecute(ServerCommandSource source, Entity senderEntity, Team team, List<ServerPlayerEntity> recipients, SignedMessage message) {
        if (ServerUtilsMod.getConfig().chat().enabled()) {
            ModUtils.sendTeamMessageFromRedirect(source, senderEntity, team, recipients, message);
        } else {
            execute(source, senderEntity, team, recipients, message);
        }
    }

    @Shadow
    private static void execute(ServerCommandSource source, Entity senderEntity, Team team, List<ServerPlayerEntity> recipients, SignedMessage message) {
        throw new IllegalStateException();
    }
}
