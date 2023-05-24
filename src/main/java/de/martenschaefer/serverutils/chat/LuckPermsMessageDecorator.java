package de.martenschaefer.serverutils.chat;

import java.util.concurrent.CompletableFuture;
import net.minecraft.network.message.MessageDecorator;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import de.martenschaefer.serverutils.ModUtils;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.PatternPlaceholderParser;
import eu.pb4.placeholders.api.parsers.TextParserV1;
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

        prefix = prefix.replaceAll("&([\\da-f])", "§$1");
        suffix = suffix.replaceAll("&([\\da-f])", "§$1");

        Text parsedMessage = NodeParser.merge(TextParserV1.SAFE, PatternPlaceholderParser.of(Placeholders.PREDEFINED_PLACEHOLDER_PATTERN, PlaceholderContext.KEY, Placeholders.DEFAULT_PLACEHOLDER_GETTER))
            .parseText(TextNode.convert(message), placeholderContext.asParserContext());

        Text result = Text.literal(prefix).append("<")
            .append(usernameFormatting == Formatting.RESET ? sender.getName().copy() : sender.getName().copy().formatted(usernameFormatting))
            .append("> ").append(Text.literal(suffix)).append(parsedMessage);
        return CompletableFuture.completedFuture(result);
    }
}
