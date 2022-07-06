package de.martenschaefer.serverutils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ContainerLock;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import de.martenschaefer.serverutils.config.ContainerLockConfig;
import de.martenschaefer.serverutils.holder.LockPermissionHolder;
import me.lucko.fabric.api.permissions.v0.Permissions;
import org.jetbrains.annotations.Nullable;

public final class ModUtils {
    private ModUtils() {
    }

    // Chat

    public static MutableText getCoordinateText(BlockPos pos) {
        return Texts.bracketed(Text.translatable("chat.coordinates", pos.getX(), pos.getY(), pos.getZ()))
            .styled(style -> style.withColor(Formatting.GREEN)
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ()))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("chat.coordinates.tooltip"))));
    }

    public static MutableText getCoordinateTextUnstyled(BlockPos pos) {
        return Texts.bracketed(Text.translatable("chat.coordinates", pos.getX(), pos.getY(), pos.getZ()));
    }

    public static void sendMessage(ServerPlayerEntity player, Text text, boolean inPublicChat) {
        if (inPublicChat) {
            MinecraftServer server = player.getServer();

            if (server == null) {
                // This can't actually happen, since this is on a server.
                player.sendMessage(text, false);
                return;
            }

            server.getPlayerManager().broadcast(text, false);
        } else {
            player.sendMessage(text, false);
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

    // Container Lock

    public static boolean canAlwaysOpen(ContainerLock lock) {
        return lock.key.isEmpty() && ((LockPermissionHolder) lock).getLockPermission().isEmpty();
    }

    public static String getLockPermission(ContainerLockConfig config, LockPermissionHolder lock) {
        String permission = lock.getLockPermission();

        if (!config.permissionPrefix().isEmpty()) {
            permission = config.permissionPrefix() + "." + permission;
        }

        permission = ServerUtilsMod.MODID + "." + permission;
        return permission;
    }

    public static boolean checkLockPermission(ContainerLockConfig config, PlayerEntity player, ContainerLock lock) {
        LockPermissionHolder lockPermission = (LockPermissionHolder) lock;

        if (lockPermission.getLockPermission().isEmpty()) {
            return lock.canOpen(player.getMainHandStack());
        } else {
            return Permissions.check(player, getLockPermission(config, lockPermission)) && lock.canOpen(player.getMainHandStack());
        }
    }
}
