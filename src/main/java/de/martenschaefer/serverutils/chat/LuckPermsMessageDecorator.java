package de.martenschaefer.serverutils.chat;

import java.util.concurrent.CompletableFuture;
import net.minecraft.network.message.MessageDecorator;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import de.martenschaefer.serverutils.ModUtils;
import de.martenschaefer.serverutils.ServerUtilsMod;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.node.NonTransformableNode;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.node.parent.FormattingNode;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.PatternPlaceholderParser;
import eu.pb4.placeholders.api.parsers.TextParserV1;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.jetbrains.annotations.Nullable;

public class LuckPermsMessageDecorator implements MessageDecorator {
    private LuckPerms api;

    public LuckPermsMessageDecorator() {
        this.api = null;
    }

    @Override
    public CompletableFuture<Text> decorate(@Nullable ServerPlayerEntity sender, Text message) {
        if (sender == null) {
            return CompletableFuture.completedFuture(message);
        }

        if (this.api == null) {
            this.api = LuckPermsProvider.get();
        }

        PlaceholderContext placeholderContext = PlaceholderContext.of(sender);
        ParserContext parserContext = placeholderContext.asParserContext();

        User user = this.api.getPlayerAdapter(ServerPlayerEntity.class).getUser(sender);
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

        NodeParser parser = NodeParser.merge(Permissions.check(sender, ServerUtilsMod.MODID + ".chat.unsafe.allow", false) ? TextParserV1.DEFAULT : TextParserV1.SAFE,
            PatternPlaceholderParser.of(Placeholders.PREDEFINED_PLACEHOLDER_PATTERN, PlaceholderContext.KEY, Placeholders.DEFAULT_PLACEHOLDER_GETTER));

        // prefix = prefix.replaceAll("&([\\da-f])", "§$1");
        // suffix = suffix.replaceAll("&([\\da-f])", "§$1");

        TextNode parsedPrefix = parser.parseNode(prefix);
        TextNode parsedSuffix = parser.parseNode(suffix);
        TextNode parsedMessage = parser.parseNode(TextNode.convert(message));

        TextNode resultNode = TextNode.wrap(parsedPrefix, new NonTransformableNode(TextNode.of("<")),
            usernameFormatting == Formatting.RESET ? TextNode.convert(sender.getName()) :
                new FormattingNode(TextNode.array(TextNode.convert(sender.getName())), usernameFormatting),
            new NonTransformableNode(TextNode.of("> ")), parsedSuffix, parsedMessage);
        return CompletableFuture.completedFuture(resultNode.toText(parserContext));
    }
}
