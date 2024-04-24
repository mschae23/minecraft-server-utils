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

package de.mschae23.serverutils.chat;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.network.message.MessageType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import com.mojang.authlib.GameProfile;
import de.mschae23.serverutils.ModUtils;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.node.NonTransformableNode;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.node.TranslatedNode;
import eu.pb4.placeholders.api.node.parent.ColorNode;
import eu.pb4.placeholders.api.node.parent.FormattingNode;
import eu.pb4.placeholders.api.node.parent.ItalicNode;
import eu.pb4.placeholders.api.parsers.NodeParser;
import net.luckperms.api.context.MutableContextSet;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LuckPermsMessageDecorator {
    private LuckPermsMessageDecorator() {
    }

    public static CompletableFuture<Text> processFromDiscord(MinecraftServer server, @NotNull UUID senderUuid, String senderName, Text message, MessageType.Parameters params) {
        return ModUtils.getLuckPerms().getUserManager().loadUser(senderUuid).thenComposeAsync(user -> process(server, user, null, Text.literal(senderName), PlaceholderContext.of(new GameProfile(senderUuid, user.getUsername()), server), message, params, true), server);
    }

    public static CompletableFuture<Text> process(@NotNull ServerPlayerEntity sender, Text message, MessageType.Parameters params) {
        return process(sender.getServerWorld().getServer(), ModUtils.getLuckPerms().getPlayerAdapter(ServerPlayerEntity.class).getUser(sender), sender, sender.getDisplayName(), PlaceholderContext.of(sender), message, params, false);
    }

    private static CompletableFuture<Text> process(MinecraftServer server, @NotNull User user, @Nullable ServerPlayerEntity sender, @NotNull Text senderName, PlaceholderContext placeholderContext, Text message, MessageType.Parameters params, boolean discord) {
        StaticMessageType type = getMessageTypeId(server.getRegistryManager(), params);
        QueryOptions previousQueryOptions = user.getQueryOptions();
        MutableContextSet context = previousQueryOptions.context().mutableCopy();
        context.add("message_type", switch (type) {
            case CHAT -> "chat";
            case SAY_COMMAND -> "say_command";
            case MSG_COMMAND_INCOMING -> "msg_command_incoming";
            case MSG_COMMAND_OUTGOING -> "msg_command_outgoing";
            case TEAM_MSG_COMMAND_INCOMING -> "team_msg_command_incoming";
            case TEAM_MSG_COMMAND_OUTGOING -> "team_msg_command_outgoing";
            case EMOTE_COMMAND -> "emote_command";
            case DEFAULT -> "default";
        });
        context.add("message_team", switch (type) {
            case TEAM_MSG_COMMAND_INCOMING, TEAM_MSG_COMMAND_OUTGOING -> "true";
            default -> "false";
        });
        context.add("message_discord", discord ? "true" : "false");

        QueryOptions options = user.getQueryOptions().toBuilder().context(context).build();

        String prefix = user.getCachedData().getMetaData(options).getPrefix();
        String suffix = user.getCachedData().getMetaData(options).getSuffix();
        String colorName = user.getCachedData().getMetaData(options).getMetaValue("username-color");
        Formatting usernameFormatting = ModUtils.getUsernameFormatting(colorName);

        return (sender == null ? ModUtils.allowUnsafeChat(user.getUniqueId()) : CompletableFuture.completedFuture(ModUtils.allowUnsafeChat(sender))).thenApplyAsync(allowUnsafeChat ->
            process(server, placeholderContext, senderName, prefix != null ? prefix : "", suffix != null ? suffix : "", usernameFormatting,
            message, params, Optional.of(type), allowUnsafeChat), server);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static Text process(MinecraftServer server, PlaceholderContext placeholderContext, Text senderName,
                               @NotNull String prefix, @NotNull String suffix, @NotNull Formatting usernameFormatting,
                               Text message,
                               MessageType.Parameters params, Optional<StaticMessageType> typeOption, boolean allowUnsafe) {
        ParserContext parserContext = placeholderContext.asParserContext();
        NodeParser parser = ModUtils.createNodeParser(allowUnsafe);

        TextNode parsedPrefix = parser.parseNode(prefix);
        TextNode parsedSuffix = parser.parseNode(suffix);
        TextNode parsedMessage = parser.parseNode(TextNode.convert(message));

        TextNode formattedName = usernameFormatting == Formatting.RESET ? TextNode.convert(senderName) :
            new FormattingNode(TextNode.array(TextNode.convert(senderName)), usernameFormatting);

        StaticMessageType type = typeOption.orElseGet(() -> getMessageTypeId(server.getRegistryManager(), params));

        TextNode resultNode;

        if (type == StaticMessageType.CHAT) {
            resultNode = TextNode.wrap(parsedPrefix, new NonTransformableNode(TextNode.of("<")), formattedName,
                new NonTransformableNode(TextNode.of("> ")), parsedSuffix, parsedMessage);
        } else if (type == StaticMessageType.MSG_COMMAND_INCOMING) {
            resultNode = new ItalicNode(TextNode.array(new ColorNode(TextNode.array(
                TextNode.of("[PM <-] "), parsedPrefix, new NonTransformableNode(TextNode.of("<")), formattedName,
                new NonTransformableNode(TextNode.of("> ")), parsedSuffix, parsedMessage),
                TextColor.fromFormatting(Formatting.GRAY))), true);
        } else if (type == StaticMessageType.MSG_COMMAND_OUTGOING && params.targetName().isPresent()) {
            resultNode = new ItalicNode(TextNode.array(new ColorNode(TextNode.array(
                TextNode.wrap(TextNode.of("[PM -> "), TextNode.convert(params.targetName().get()), TextNode.of("] ")),
                parsedPrefix, new NonTransformableNode(TextNode.of("<")), formattedName,
                new NonTransformableNode(TextNode.of("> ")), parsedSuffix, parsedMessage),
                TextColor.fromFormatting(Formatting.GRAY))), true);
        } else if (type == StaticMessageType.MSG_COMMAND_OUTGOING) {
            resultNode = new ItalicNode(TextNode.array(new ColorNode(TextNode.array(
                TextNode.of("[PM ->] "), parsedPrefix, new NonTransformableNode(TextNode.of("<")), formattedName,
                new NonTransformableNode(TextNode.of("> ")), parsedSuffix, parsedMessage),
                TextColor.fromFormatting(Formatting.GRAY))), true);
        } else if (type == StaticMessageType.TEAM_MSG_COMMAND_INCOMING && params.targetName().isPresent()) {
            resultNode = TextNode.wrap(
                new ColorNode(TextNode.array(TextNode.of("[TM <- "), TextNode.convert(params.targetName().get()), TextNode.of("] ")),
                    TextColor.fromFormatting(Formatting.GRAY)),
                parsedPrefix, new NonTransformableNode(TextNode.of("<")), formattedName,
                new NonTransformableNode(TextNode.of("> ")), parsedSuffix, parsedMessage);
        } else if (type == StaticMessageType.TEAM_MSG_COMMAND_INCOMING) {
            resultNode = TextNode.wrap(
                new ColorNode(TextNode.array(TextNode.of("[TM <-] ")), TextColor.fromFormatting(Formatting.GRAY)),
                parsedPrefix, new NonTransformableNode(TextNode.of("<")), formattedName,
                new NonTransformableNode(TextNode.of("> ")), parsedSuffix, parsedMessage);
        } else if (type == StaticMessageType.TEAM_MSG_COMMAND_OUTGOING && params.targetName().isPresent()) {
            resultNode = TextNode.wrap(
                new ColorNode(TextNode.array(TextNode.of("[TM -> "), TextNode.convert(params.targetName().get()), TextNode.of("] ")),
                    TextColor.fromFormatting(Formatting.GRAY)),
                parsedPrefix, new NonTransformableNode(TextNode.of("<")), formattedName,
                new NonTransformableNode(TextNode.of("> ")), parsedSuffix, parsedMessage);
        }  else if (type == StaticMessageType.TEAM_MSG_COMMAND_OUTGOING) {
            resultNode = TextNode.wrap(
                new ColorNode(TextNode.array(TextNode.of("[TM ->] ")), TextColor.fromFormatting(Formatting.GRAY)),
                parsedPrefix, new NonTransformableNode(TextNode.of("<")), formattedName,
                new NonTransformableNode(TextNode.of("> ")), parsedSuffix, parsedMessage);
        } else if (type == StaticMessageType.EMOTE_COMMAND) {
            resultNode = TranslatedNode.of("chat.type.emote", formattedName, parsedMessage);
        } else { //  if (type == StaticMessageType.SAY_COMMAND) { // use /say format by default
            resultNode = TextNode.wrap(parsedPrefix, new NonTransformableNode(TextNode.of("[")), formattedName,
                new NonTransformableNode(TextNode.of("] ")), parsedSuffix, parsedMessage);
        }

        return resultNode.toText(parserContext);
    }

    private static StaticMessageType getMessageTypeId(DynamicRegistryManager manager, MessageType.Parameters params) {
        return params.type().getKey().or(() -> manager.get(RegistryKeys.MESSAGE_TYPE).getKey(params.type().value()))
            .map(key -> {
                if (!"minecraft".equals(key.getValue().getNamespace())) {
                    return StaticMessageType.DEFAULT;
                }

                return switch (key.getValue().getPath()) {
                    case "chat" -> StaticMessageType.CHAT;
                    case "say_command" -> StaticMessageType.SAY_COMMAND;
                    case "msg_command_incoming" -> StaticMessageType.MSG_COMMAND_INCOMING;
                    case "msg_command_outgoing" -> StaticMessageType.MSG_COMMAND_OUTGOING;
                    case "team_msg_command_incoming" -> StaticMessageType.TEAM_MSG_COMMAND_INCOMING;
                    case "team_msg_command_outgoing" -> StaticMessageType.TEAM_MSG_COMMAND_OUTGOING;
                    case "emote_command" -> StaticMessageType.EMOTE_COMMAND;
                    default -> StaticMessageType.DEFAULT;
                };
            }).orElse(StaticMessageType.DEFAULT);
    }

    public enum StaticMessageType {
        CHAT,
        SAY_COMMAND,
        MSG_COMMAND_INCOMING,
        MSG_COMMAND_OUTGOING,
        TEAM_MSG_COMMAND_INCOMING,
        TEAM_MSG_COMMAND_OUTGOING,
        EMOTE_COMMAND,
        DEFAULT,
    }
}
