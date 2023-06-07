package de.martenschaefer.serverutils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ContainerLock;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TeamMsgCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import de.martenschaefer.serverutils.chat.LuckPermsMessageDecorator;
import de.martenschaefer.serverutils.config.ContainerLockConfig;
import de.martenschaefer.serverutils.holder.LockPermissionHolder;
import de.martenschaefer.serverutils.state.PlayerTeamStorageContainer;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.PatternPlaceholderParser;
import eu.pb4.placeholders.api.parsers.TextParserV1;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
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
            MinecraftServer server = player.getWorld().getServer();
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

    public static Formatting getUsernameFormatting(@NotNull CachedMetaData data) {
        String colorName = data.getMetaValue("username-color");
        return getUsernameFormatting(colorName);
    }

    public static Formatting getUsernameFormatting(@NotNull ServerPlayerEntity player) {
        return getUsernameFormatting(getLuckPerms().getPlayerAdapter(ServerPlayerEntity.class).getMetaData(player));
    }

    public static void updateUsernameFormatting(MinecraftServer server, ServerPlayerEntity player, CachedMetaData data) {
        Formatting usernameFormatting = getUsernameFormatting(data);

        server.getPlayerManager().sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player));
        ((PlayerTeamStorageContainer) server.getPlayerManager()).getPlayerTeamStorage().updateFormatting(player, usernameFormatting);
    }

    public static NodeParser createNodeParser(boolean allowUnsafe) {
        return NodeParser.merge(allowUnsafe ? TextParserV1.DEFAULT : TextParserV1.SAFE,
            PatternPlaceholderParser.of(Placeholders.PREDEFINED_PLACEHOLDER_PATTERN, PlaceholderContext.KEY, Placeholders.DEFAULT_PLACEHOLDER_GETTER));
    }

    public static boolean allowUnsafeChat(@Nullable ServerPlayerEntity player) {
        return player != null && Permissions.check(player, ServerUtilsMod.MODID + ".chat.unsafe.allow", false);
    }

    public static NodeParser createNodeParser(@Nullable ServerPlayerEntity player) {
        return createNodeParser(allowUnsafeChat(player));
    }

    public static Text decorateText(Text message, ServerCommandSource source, MessageType.Parameters params) {
        if (source.getPlayer() != null) {
            return LuckPermsMessageDecorator.process(source.getPlayer(), message, params);
        } else {
            return LuckPermsMessageDecorator.processFromCommandSource(source, source.getDisplayName(), "", "", Formatting.RESET,
                message, params, Optional.empty(), source.hasPermissionLevel(3));
        }
    }

    public static void broadcastPlayerChatMessageFromRedirect(PlayerManager manager, SignedMessage message, ServerPlayerEntity sender, MessageType.Parameters params) {
        boolean verified = manager.verify(message);
        Text decoratedMessage = LuckPermsMessageDecorator.process(sender, message.getContent(), params);
        Text loggedText = verified ? decoratedMessage : Text.literal("[Not Secure] ").append(decoratedMessage);

        manager.broadcast(loggedText, player -> decoratedMessage, false);
    }

    public static void broadcastSourceChatMessageFromRedirect(PlayerManager manager, SignedMessage message, ServerCommandSource source, MessageType.Parameters params) {
        if (source.getPlayer() != null) {
            broadcastPlayerChatMessageFromRedirect(manager, message, source.getPlayer(), params);
            return;
        }

        boolean verified = manager.verify(message);
        Text decoratedMessage = LuckPermsMessageDecorator.processFromCommandSource(source, source.getDisplayName(), "", "", Formatting.RESET,
            message.getContent(), params, Optional.empty(), source.hasPermissionLevel(3));
        Text loggedText = verified ? decoratedMessage : Text.literal("[Not Secure] ").append(decoratedMessage);

        manager.broadcast(loggedText, player -> decoratedMessage, false);
    }

    public static void sendPrivateMessageFromRedirect(ServerCommandSource source, Collection<ServerPlayerEntity> targets, SignedMessage message) {
        MessageType.Parameters incomingParams = MessageType.params(MessageType.MSG_COMMAND_INCOMING, source);
        Text decoratedIncomingMessage = ModUtils.decorateText(message.getContent(), source, incomingParams);

        for(ServerPlayerEntity target : targets) {
            MessageType.Parameters outgoingParams = MessageType.params(MessageType.MSG_COMMAND_OUTGOING, source)
                .withTargetName(target.getDisplayName());
            Text decoratedOutgoingMessage = ModUtils.decorateText(message.getContent(), source, outgoingParams);

            source.sendMessage(decoratedOutgoingMessage);
            target.sendMessage(decoratedIncomingMessage, false);
        }
    }

    public static void sendTeamMessageFromRedirect(ServerCommandSource source, Entity senderEntity, Team team, List<ServerPlayerEntity> recipients, SignedMessage message) {
        MutableText formattedTeamName = team.getDisplayName().copy().fillStyle(Style.EMPTY.withInsertion(team.getName()).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(team.getName()))));
        Formatting teamColor = team.getColor();

        if (teamColor != Formatting.RESET) {
            formattedTeamName.formatted(teamColor);
        }

        formattedTeamName.fillStyle(TeamMsgCommand.STYLE);
        MessageType.Parameters incomingParams = MessageType.params(MessageType.TEAM_MSG_COMMAND_INCOMING, source).withTargetName(formattedTeamName);
        MessageType.Parameters outgoingParams = MessageType.params(MessageType.TEAM_MSG_COMMAND_OUTGOING, source).withTargetName(formattedTeamName);

        Text decoratedIncomingMessage = ModUtils.decorateText(message.getContent(), source, incomingParams);
        Text decoratedOutgoingMessage = ModUtils.decorateText(message.getContent(), source, outgoingParams);

        for(ServerPlayerEntity target : recipients) {
            target.sendMessage(target == senderEntity ? decoratedOutgoingMessage : decoratedIncomingMessage, false);
        }
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

    public static LuckPerms getLuckPerms() {
        if (luckPerms == null) {
            luckPerms = LuckPermsProvider.get();
        }

        return luckPerms;
    }
}
