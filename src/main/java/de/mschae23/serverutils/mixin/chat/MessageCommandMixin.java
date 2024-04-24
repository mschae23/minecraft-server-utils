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

import java.util.Collection;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.command.MessageCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import de.mschae23.serverutils.ModUtils;
import de.mschae23.serverutils.ServerUtilsMod;
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
