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
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ModUtils {
    private static LuckPerms luckPerms = null;

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
        Formatting usernameFormatting = Formatting.RESET;

        if (colorName != null) {
            Formatting formatting = Formatting.byName(colorName);

            if (formatting != null) {
                usernameFormatting = formatting;
            }
        }

        return usernameFormatting;
    }

    public static Formatting getUsernameFormatting(@NotNull ServerPlayerEntity player) {
        User user = getLuckPerms().getPlayerAdapter(ServerPlayerEntity.class).getUser(player);
        String colorName = user.getCachedData().getMetaData().getMetaValue("username-color");
        Formatting formatting = Formatting.byName(colorName);

        if (formatting == null) {
            formatting = Formatting.RESET;
        }

        return formatting;
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

    private static LuckPerms getLuckPerms() {
        if (luckPerms == null) {
            luckPerms = LuckPermsProvider.get();
        }

        return luckPerms;
    }
}
