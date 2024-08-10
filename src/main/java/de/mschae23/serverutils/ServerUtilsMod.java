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

package de.mschae23.serverutils;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;
import net.minecraft.command.CommandSource;
import net.minecraft.network.message.MessageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.GlobalPos;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import de.martenschaefer.config.api.ConfigIo;
import de.martenschaefer.config.api.ModConfig;
import de.mschae23.serverutils.command.LockCommand;
import de.mschae23.serverutils.command.PosCommand;
import de.mschae23.serverutils.command.ServerUtilsCommand;
import de.mschae23.serverutils.command.UnlockCommand;
import de.mschae23.serverutils.command.VoteCommand;
import de.mschae23.serverutils.config.ServerUtilsConfigV6;
import de.mschae23.serverutils.config.v1.ServerUtilsConfigV1;
import de.mschae23.serverutils.config.v2.ServerUtilsConfigV2;
import de.mschae23.serverutils.config.v3.ServerUtilsConfigV3;
import de.mschae23.serverutils.config.v4.ServerUtilsConfigV4;
import de.mschae23.serverutils.config.v5.ServerUtilsConfigV5;
import de.mschae23.serverutils.registry.ServerUtilsRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerUtilsMod implements ModInitializer {
    public static final String MODID = "serverutils";
    @SuppressWarnings("unused")
    public static final Logger LOGGER = LoggerFactory.getLogger("Server utils");

    private static final ServerUtilsConfigV6 LATEST_CONFIG_DEFAULT = ServerUtilsConfigV6.DEFAULT;
    private static final int LATEST_CONFIG_VERSION = LATEST_CONFIG_DEFAULT.version();
    private static final Codec<ModConfig<ServerUtilsConfigV6>> CONFIG_CODEC = ModConfig.createCodec(LATEST_CONFIG_VERSION, ServerUtilsMod::getConfigType);

    private static ServerUtilsConfigV6 CONFIG = LATEST_CONFIG_DEFAULT;

    public static final RegistryKey<MessageType> UNDECORATED_CHAT = RegistryKey.of(RegistryKeys.MESSAGE_TYPE, id("undecorated_chat"));

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(server ->
            CONFIG = ConfigIo.initializeConfig(Paths.get(MODID + ".json"), LATEST_CONFIG_VERSION, LATEST_CONFIG_DEFAULT, CONFIG_CODEC,
                RegistryOps.of(JsonOps.INSTANCE, server.getRegistryManager()), LOGGER::info, LOGGER::error)
        );

        ServerUtilsRegistries.init();

        // Death Coordinates
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            var config = getConfig().misc().deathCoords();

            if (alive || !Permissions.check(newPlayer, MODID + ".death.printcoords.enabled", config.enabled())) {
                return;
            }

            boolean inPublicChat = Permissions.check(newPlayer, MODID + ".death.printcoords.public", config.inPublicChat());
            GlobalPos deathPos = newPlayer.getLastDeathPos().orElseGet(() -> GlobalPos.create(oldPlayer.getWorld().getRegistryKey(), oldPlayer.getBlockPos()));

            MutableText text = Text.empty().append(oldPlayer.getDisplayName().copy()).append(Text.literal(" died at "))
                .append(ModUtils.getCoordinateText(deathPos.getPos()));

            if (newPlayer.getLastDeathPos().isPresent()) {
                text.append(" in ").append(Text.literal(deathPos.getDimension().getValue().toString()).formatted(Formatting.YELLOW));
            }

            if (inPublicChat) {
                // If in_public_chat is true, send message to everyone
                newPlayer.getServerWorld().getServer().getPlayerManager().broadcast(text, false);
            }

            // Otherwise, send it to the player directly.
            // Because COPY_FROM is called in a state where neither the old nor new player are in the
            // player manager's "players" list, the message needs to be manually sent to the player that died
            // even if in_public_chat is true.
            newPlayer.sendMessage(text, false);
        });

        // Command registration
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ServerUtilsCommand.register(dispatcher);
            PosCommand.register(dispatcher);
            LockCommand.register(dispatcher);
            UnlockCommand.register(dispatcher);
            VoteCommand.register(dispatcher);
        });

        // Check permissions on the server once, so that they are registered and can be auto-completed
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            CommandSource source = server.getCommandSource();

            Stream<String> commandPermissions = Arrays.stream(new String[][] {
                ServerUtilsCommand.PERMISSIONS,
                PosCommand.PERMISSIONS,
                LockCommand.PERMISSIONS,
                UnlockCommand.PERMISSIONS,
                VoteCommand.PERMISSIONS,
            }).flatMap(Arrays::stream);

            String[] permissions = new String[] {
                ".death.printcoords.enabled",
                ".death.printcoords.public",
            };

            Stream.concat(commandPermissions, Arrays.stream(permissions))
                .forEach(permission -> Permissions.check(source, MODID + permission));

            ModUtils.getLuckPerms().getContextManager().registerCalculator(new ContextCalculator<ServerPlayerEntity>() {
                @Override
                public void calculate(@NotNull ServerPlayerEntity target, @NotNull ContextConsumer contextConsumer) {
                }

                @Override
                public @NotNull ContextSet estimatePotentialContexts() {
                    ImmutableContextSet.Builder builder = ImmutableContextSet.builder();
                    builder.add("message_type", "chat");
                    builder.add("message_type", "say_command");
                    builder.add("message_type", "msg_command_incoming");
                    builder.add("message_type", "msg_command_outgoing");
                    builder.add("message_type", "team_msg_command_incoming");
                    builder.add("message_type", "team_msg_command_outgoing");
                    builder.add("message_type", "emote_command");
                    builder.add("message_type", "default");

                    builder.add("message_team", "true");
                    builder.add("message_team", "false");

                    builder.add("message_discord", "true");
                    builder.add("message_discord", "false");
                    return builder.build();
                }
            });
        });

        //noinspection resource
        ServerLifecycleEvents.SERVER_STARTED.register(server ->
            ModUtils.getLuckPerms().getEventBus().subscribe(UserDataRecalculateEvent.class, event -> onUserDataRecalculate(server, event)));
    }

    private static void onUserDataRecalculate(MinecraftServer server, UserDataRecalculateEvent event) {
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(event.getUser().getUniqueId());

        if (player != null) {
            ModUtils.updateUsernameFormatting(server, player, event.getData().getMetaData());
        }
    }

    @SuppressWarnings("deprecation")
    private static ModConfig.Type<ServerUtilsConfigV6, ?> getConfigType(int version) {
        return new ModConfig.Type<>(version, switch (version) {
            case 1 -> ServerUtilsConfigV1.TYPE_CODEC;
            case 2 -> ServerUtilsConfigV2.TYPE_CODEC;
            case 3 -> ServerUtilsConfigV3.TYPE_CODEC;
            case 4 -> ServerUtilsConfigV4.TYPE_CODEC;
            case 5 -> ServerUtilsConfigV5.TYPE_CODEC;
            default -> ServerUtilsConfigV6.TYPE_CODEC;
        });
    }

    public static ServerUtilsConfigV6 getConfig() {
        return CONFIG;
    }

    public static Identifier id(String path) {
        return Identifier.of(MODID, path);
    }
}
