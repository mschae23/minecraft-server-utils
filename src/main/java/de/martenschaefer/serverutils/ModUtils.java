package de.martenschaefer.serverutils;

import net.minecraft.network.message.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public final class ModUtils {
    private ModUtils() {
    }

    public static MutableText getCoordinateText(BlockPos pos) {
        return Texts.bracketed(Text.translatable("chat.coordinates", pos.getX(), pos.getY(), pos.getZ()))
            .styled(style -> style.withColor(Formatting.GREEN)
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ()))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("chat.coordinates.tooltip"))));
    }

    public static void sendMessage(ServerPlayerEntity player, Text text, boolean inPublicChat) {
        if (inPublicChat) {
            MinecraftServer server = player.getServer();

            if (server == null) {
                // This can't actually happen, since this is on a server.
                player.sendMessage(text, MessageType.SYSTEM);
                return;
            }

            server.getPlayerManager().broadcast(text, MessageType.SYSTEM);
        } else {
            player.sendMessage(text, MessageType.SYSTEM);
        }
    }

    public static Formatting getUsernameFormatting(@Nullable String colorName) {
        Formatting usernameFormatting = Formatting.WHITE;

        if (colorName != null) {
            Formatting formatting = Formatting.byName(colorName);

            if (formatting != null) {
                usernameFormatting = formatting;
            }
        }

        return usernameFormatting;
    }
}
