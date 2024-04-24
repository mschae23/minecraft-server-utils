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

import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import de.mschae23.serverutils.ModUtils;
import de.mschae23.serverutils.ServerUtilsMod;
import de.mschae23.serverutils.event.BroadcastMessageEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    /**
     * @author mschae23
     * @reason This is a private method that gets a nullable player; there are two methods that can call it,
     *         which take a ServerCommandSource or ServerPlayerEntity, respectively. That's more useful to work with,
     *         so this method is changed to just crash in case it gets called anyway, so I get notified in case something doesn't work.
     */
    @Inject(method = "broadcast(Lnet/minecraft/network/message/SignedMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/MessageType$Parameters;)V", at = @At("HEAD"))
    private void onInternalBroadcast(SignedMessage message, Predicate<ServerPlayerEntity> shouldSendFiltered, @Nullable ServerPlayerEntity sender, MessageType.Parameters params, CallbackInfo ci) {
        if (ServerUtilsMod.getConfig().chat().enabled()) {
            throw new UnsupportedOperationException("Private method; the two public methods that call it were changed, so this method shouldn't have been called");
        }
    }

    @Redirect(method = "broadcast(Lnet/minecraft/network/message/SignedMessage;Lnet/minecraft/server/command/ServerCommandSource;Lnet/minecraft/network/message/MessageType$Parameters;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/network/message/SignedMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/MessageType$Parameters;)V"))
    private void redirectBroadcastSource(PlayerManager manager, SignedMessage message2, Predicate<ServerPlayerEntity> shouldSendFiltered, @Nullable ServerPlayerEntity sender, MessageType.Parameters params2, SignedMessage message, ServerCommandSource source, MessageType.Parameters params) {
        if (ServerUtilsMod.getConfig().chat().enabled()) {
            ModUtils.broadcastSourceChatMessageFromRedirect(manager, message, source, params);
        } else {
            broadcast(message2, shouldSendFiltered, sender, params2);
        }
    }

    @Redirect(method = "broadcast(Lnet/minecraft/network/message/SignedMessage;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/MessageType$Parameters;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/network/message/SignedMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/MessageType$Parameters;)V"))
    private void redirectBroadcastPlayer(PlayerManager manager, SignedMessage message2, Predicate<ServerPlayerEntity> shouldSendFiltered, ServerPlayerEntity sender2, MessageType.Parameters params2, SignedMessage message, ServerPlayerEntity sender, MessageType.Parameters params) {
        if (ServerUtilsMod.getConfig().chat().enabled()) {
            ModUtils.broadcastPlayerChatMessageFromRedirect(manager, message, sender, params);
        } else {
            broadcast(message2, shouldSendFiltered, sender2, params2);
        }
    }

    @Inject(method = "broadcast(Lnet/minecraft/text/Text;Ljava/util/function/Function;Z)V", at = @At("TAIL"))
    private void injectAtBroadcast(Text message, Function<ServerPlayerEntity, Text> playerMessageFactory, boolean overlay, CallbackInfo ci) {
        BroadcastMessageEvent.EVENT.invoker().announce(this.getServer(), message);
    }

    @Shadow
    private void broadcast(SignedMessage message, Predicate<ServerPlayerEntity> shouldSendFiltered, @Nullable ServerPlayerEntity sender, MessageType.Parameters params) {
        throw new IllegalStateException();
    }

    @Shadow
    public abstract MinecraftServer getServer();
}
