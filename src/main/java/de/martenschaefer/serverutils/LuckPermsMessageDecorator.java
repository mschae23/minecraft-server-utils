package de.martenschaefer.serverutils;

import java.util.concurrent.CompletableFuture;
import net.minecraft.network.message.MessageDecorator;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.jetbrains.annotations.Nullable;

public class LuckPermsMessageDecorator implements MessageDecorator {
    private LuckPerms api;

    LuckPermsMessageDecorator() {
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

        User user = this.api.getPlayerAdapter(ServerPlayerEntity.class).getUser(sender);
        String prefix = user.getCachedData().getMetaData().getPrefix();
        String suffix = user.getCachedData().getMetaData().getSuffix();
        String colorName = user.getCachedData().getMetaData().getMetaValue("username-color");
        Formatting usernameFormatting = Formatting.WHITE;

        if (prefix == null) {
            prefix = "";
        }

        if (suffix == null) {
            suffix = "";
        }

        if (colorName != null) {
            Formatting formatting = Formatting.byName(colorName);

            if (formatting != null) {
                usernameFormatting = formatting;
            }
        }

        prefix = prefix.replaceAll("&([\\da-f])", "§$1");
        suffix = suffix.replaceAll("&([\\da-f])", "§$1");

        Text result = Text.literal(prefix).append("<")
            .append(sender.getName().copy().append(Text.literal(suffix)).formatted(usernameFormatting))
            .append("> ").append(message);
        return CompletableFuture.completedFuture(result);
    }
}
