package de.martenschaefer.serverutils.chat;

import java.util.Optional;
import net.minecraft.network.message.MessageType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import de.martenschaefer.serverutils.ModUtils;
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

public final class LuckPermsMessageDecorator {
    private LuckPermsMessageDecorator() {
    }

    public static Text process(@NotNull ServerPlayerEntity sender, Text message, MessageType.Parameters params) {
        User user = ModUtils.getLuckPerms().getPlayerAdapter(ServerPlayerEntity.class).getUser(sender);

        StaticMessageType type = getMessageTypeId(sender.getWorld().getRegistryManager(), params);
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

        QueryOptions options = user.getQueryOptions().toBuilder().context(context).build();

        String prefix = user.getCachedData().getMetaData(options).getPrefix();
        String suffix = user.getCachedData().getMetaData(options).getSuffix();
        String colorName = user.getCachedData().getMetaData(options).getMetaValue("username-color");
        Formatting usernameFormatting = ModUtils.getUsernameFormatting(colorName);

        if (prefix == null) {
            prefix = "";
        }

        if (suffix == null) {
            suffix = "";
        }

        return processFromCommandSource(sender.getCommandSource(), sender.getDisplayName(), prefix, suffix, usernameFormatting,
            message, params, Optional.of(type), ModUtils.allowUnsafeChat(sender));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static Text processFromCommandSource(@NotNull ServerCommandSource source, Text senderName,
                                                @NotNull String prefix, @NotNull String suffix, @NotNull Formatting usernameFormatting,
                                                Text message,
                                                MessageType.Parameters params, Optional<StaticMessageType> typeOption, boolean allowUnsafe) {
        PlaceholderContext placeholderContext = PlaceholderContext.of(source);
        ParserContext parserContext = placeholderContext.asParserContext();
        NodeParser parser = ModUtils.createNodeParser(allowUnsafe);

        TextNode parsedPrefix = parser.parseNode(prefix);
        TextNode parsedSuffix = parser.parseNode(suffix);
        TextNode parsedMessage = parser.parseNode(TextNode.convert(message));

        TextNode formattedName = usernameFormatting == Formatting.RESET ? TextNode.convert(senderName) :
            new FormattingNode(TextNode.array(TextNode.convert(senderName)), usernameFormatting);

        StaticMessageType type = typeOption.orElseGet(() -> getMessageTypeId(source.getWorld().getRegistryManager(), params));

        TextNode resultNode;

        if (type == StaticMessageType.CHAT) {
            resultNode = TextNode.wrap(parsedPrefix, new NonTransformableNode(TextNode.of("<")), formattedName,
                new NonTransformableNode(TextNode.of("> ")), parsedSuffix, parsedMessage);
        } else if (type == StaticMessageType.MSG_COMMAND_INCOMING) {
            resultNode = new ItalicNode(TextNode.array(new ColorNode(TextNode.array(
                TextNode.of("[PM <-] "), parsedPrefix, new NonTransformableNode(TextNode.of("<")), formattedName,
                new NonTransformableNode(TextNode.of("> ")), parsedSuffix, parsedMessage),
                TextColor.fromFormatting(Formatting.GRAY))), true);
        } else if (type == StaticMessageType.MSG_COMMAND_OUTGOING && params.targetName() != null) {
            resultNode = new ItalicNode(TextNode.array(new ColorNode(TextNode.array(
                TextNode.wrap(TextNode.of("[PM -> "), TextNode.convert(params.targetName()), TextNode.of("] ")),
                parsedPrefix, new NonTransformableNode(TextNode.of("<")), formattedName,
                new NonTransformableNode(TextNode.of("> ")), parsedSuffix, parsedMessage),
                TextColor.fromFormatting(Formatting.GRAY))), true);
        } else if (type == StaticMessageType.MSG_COMMAND_OUTGOING) {
            resultNode = new ItalicNode(TextNode.array(new ColorNode(TextNode.array(
                TextNode.of("[PM ->] "), parsedPrefix, new NonTransformableNode(TextNode.of("<")), formattedName,
                new NonTransformableNode(TextNode.of("> ")), parsedSuffix, parsedMessage),
                TextColor.fromFormatting(Formatting.GRAY))), true);
        } else if (type == StaticMessageType.TEAM_MSG_COMMAND_INCOMING && params.targetName() != null) {
            resultNode = TextNode.wrap(
                new ColorNode(TextNode.array(TextNode.of("[TM <- "), TextNode.convert(params.targetName()), TextNode.of("] ")),
                    TextColor.fromFormatting(Formatting.GRAY)),
                parsedPrefix, new NonTransformableNode(TextNode.of("<")), formattedName,
                new NonTransformableNode(TextNode.of("> ")), parsedSuffix, parsedMessage);
        } else if (type == StaticMessageType.TEAM_MSG_COMMAND_INCOMING) {
            resultNode = TextNode.wrap(
                new ColorNode(TextNode.array(TextNode.of("[TM <-] ")), TextColor.fromFormatting(Formatting.GRAY)),
                parsedPrefix, new NonTransformableNode(TextNode.of("<")), formattedName,
                new NonTransformableNode(TextNode.of("> ")), parsedSuffix, parsedMessage);
        } else if (type == StaticMessageType.TEAM_MSG_COMMAND_OUTGOING && params.targetName() != null) {
            resultNode = TextNode.wrap(
                new ColorNode(TextNode.array(TextNode.of("[TM -> "), TextNode.convert(params.targetName()), TextNode.of("] ")),
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
        return manager.get(RegistryKeys.MESSAGE_TYPE).getKey(params.type())
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

    private enum StaticMessageType {
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
