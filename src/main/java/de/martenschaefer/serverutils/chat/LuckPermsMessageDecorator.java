package de.martenschaefer.serverutils.chat;

import net.minecraft.network.message.MessageType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
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
import net.luckperms.api.model.user.User;
import org.jetbrains.annotations.NotNull;

public final class LuckPermsMessageDecorator {
    private LuckPermsMessageDecorator() {
    }

    public static Text process(@NotNull ServerPlayerEntity sender, Text message, MessageType.Parameters params) {
        User user = ModUtils.getLuckPerms().getPlayerAdapter(ServerPlayerEntity.class).getUser(sender);
        String prefix = user.getCachedData().getMetaData().getPrefix();
        String suffix = user.getCachedData().getMetaData().getSuffix();
        String colorName = user.getCachedData().getMetaData().getMetaValue("username-color");
        Formatting usernameFormatting = ModUtils.getUsernameFormatting(colorName);

        if (prefix == null) {
            prefix = "";
        }

        if (suffix == null) {
            suffix = "";
        }

        return processFromCommandSource(sender.getCommandSource(), sender.getDisplayName(), prefix, suffix, usernameFormatting,
            message, params, ModUtils.allowUnsafeChat(sender));
    }

    public static Text processFromCommandSource(@NotNull ServerCommandSource source, Text senderName,
                                                @NotNull String prefix, @NotNull String suffix, @NotNull Formatting usernameFormatting,
                                                Text message, MessageType.Parameters params, boolean allowUnsafe) {
        PlaceholderContext placeholderContext = PlaceholderContext.of(source);
        ParserContext parserContext = placeholderContext.asParserContext();
        NodeParser parser = ModUtils.createNodeParser(allowUnsafe);

        TextNode parsedPrefix = parser.parseNode(prefix);
        TextNode parsedSuffix = parser.parseNode(suffix);
        TextNode parsedMessage = parser.parseNode(TextNode.convert(message));

        TextNode formattedName = usernameFormatting == Formatting.RESET ? TextNode.convert(senderName) :
            new FormattingNode(TextNode.array(TextNode.convert(senderName)), usernameFormatting);

        Identifier typeId = source.getWorld().getRegistryManager().get(RegistryKeys.MESSAGE_TYPE).getKey(params.type())
            .orElse(MessageType.SAY_COMMAND).getValue();

        TextNode resultNode;

        if (MessageType.CHAT.getValue().equals(typeId)) {
            resultNode = TextNode.wrap(parsedPrefix, new NonTransformableNode(TextNode.of("<")), formattedName,
                new NonTransformableNode(TextNode.of("> ")), parsedSuffix, parsedMessage);
        } else if (MessageType.MSG_COMMAND_INCOMING.getValue().equals(typeId)) {
            resultNode = new ItalicNode(TextNode.array(new ColorNode(TextNode.array(
                TextNode.of("[PM <-] "), parsedPrefix, new NonTransformableNode(TextNode.of("<")), formattedName,
                new NonTransformableNode(TextNode.of("> ")), parsedSuffix, parsedMessage),
                TextColor.fromFormatting(Formatting.GRAY))), true);
        } else if (MessageType.MSG_COMMAND_OUTGOING.getValue().equals(typeId) && params.targetName() != null) {
            resultNode = new ItalicNode(TextNode.array(new ColorNode(TextNode.array(
                TextNode.wrap(TextNode.of("[PM -> "), TextNode.convert(params.targetName()), TextNode.of("] ")),
                parsedPrefix, new NonTransformableNode(TextNode.of("<")), formattedName,
                new NonTransformableNode(TextNode.of("> ")), parsedSuffix, parsedMessage),
                TextColor.fromFormatting(Formatting.GRAY))), true);
        } else if (MessageType.MSG_COMMAND_OUTGOING.getValue().equals(typeId)) {
            resultNode = new ItalicNode(TextNode.array(new ColorNode(TextNode.array(
                TextNode.of("[PM ->] "), parsedPrefix, new NonTransformableNode(TextNode.of("<")), formattedName,
                new NonTransformableNode(TextNode.of("> ")), parsedSuffix, parsedMessage),
                TextColor.fromFormatting(Formatting.GRAY))), true);
        } else if (MessageType.TEAM_MSG_COMMAND_INCOMING.getValue().equals(typeId) && params.targetName() != null) {
            resultNode = TextNode.wrap(
                new ColorNode(TextNode.array(TextNode.of("[TM <- "), TextNode.convert(params.targetName()), TextNode.of("] ")),
                    TextColor.fromFormatting(Formatting.GRAY)),
                parsedPrefix, new NonTransformableNode(TextNode.of("<")), formattedName,
                new NonTransformableNode(TextNode.of("> ")), parsedSuffix, parsedMessage);
        } else if (MessageType.TEAM_MSG_COMMAND_INCOMING.getValue().equals(typeId)) {
            resultNode = TextNode.wrap(
                new ColorNode(TextNode.array(TextNode.of("[TM <-] ")), TextColor.fromFormatting(Formatting.GRAY)),
                parsedPrefix, new NonTransformableNode(TextNode.of("<")), formattedName,
                new NonTransformableNode(TextNode.of("> ")), parsedSuffix, parsedMessage);
        } else if (MessageType.TEAM_MSG_COMMAND_OUTGOING.getValue().equals(typeId) && params.targetName() != null) {
            resultNode = TextNode.wrap(
                new ColorNode(TextNode.array(TextNode.of("[TM -> "), TextNode.convert(params.targetName()), TextNode.of("] ")),
                    TextColor.fromFormatting(Formatting.GRAY)),
                parsedPrefix, new NonTransformableNode(TextNode.of("<")), formattedName,
                new NonTransformableNode(TextNode.of("> ")), parsedSuffix, parsedMessage);
        }  else if (MessageType.TEAM_MSG_COMMAND_OUTGOING.getValue().equals(typeId)) {
            resultNode = TextNode.wrap(
                new ColorNode(TextNode.array(TextNode.of("[TM ->] ")), TextColor.fromFormatting(Formatting.GRAY)),
                parsedPrefix, new NonTransformableNode(TextNode.of("<")), formattedName,
                new NonTransformableNode(TextNode.of("> ")), parsedSuffix, parsedMessage);
        } else if (MessageType.EMOTE_COMMAND.getValue().equals(typeId)) {
            resultNode = TranslatedNode.of("chat.type.emote", formattedName, parsedMessage);
        } else { //  if (MessageType.SAY_COMMAND.getValue().equals(typeId)) { // use /say format by default
            resultNode = TextNode.wrap(parsedPrefix, new NonTransformableNode(TextNode.of("[")), formattedName,
                new NonTransformableNode(TextNode.of("] ")), parsedSuffix, parsedMessage);
        }

        return resultNode.toText(parserContext);
    }
}
