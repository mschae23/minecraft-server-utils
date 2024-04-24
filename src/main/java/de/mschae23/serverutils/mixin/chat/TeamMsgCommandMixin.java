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

package de.mschae23.serverutils.mixin.chat;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TeamMsgCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import de.mschae23.serverutils.ModUtils;
import de.mschae23.serverutils.ServerUtilsMod;
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
